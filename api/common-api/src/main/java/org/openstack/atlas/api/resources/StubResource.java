package org.openstack.atlas.api.resources;

import java.io.UnsupportedEncodingException;
import java.math.BigInteger;
import java.security.KeyPair;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.bouncycastle.jce.PKCS10CertificationRequest;
import org.openstack.atlas.cfg.PublicApiServiceConfigurationKeys;
import org.openstack.atlas.docs.loadbalancers.api.v1.Created;
import org.openstack.atlas.docs.loadbalancers.api.v1.SourceAddresses;
import java.util.Calendar;
import org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancerUsage;
import org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancers;
import org.openstack.atlas.docs.loadbalancers.api.v1.NetworkItem;
import org.openstack.atlas.docs.loadbalancers.api.v1.AccessList;
import java.util.ArrayList;
import java.util.Date;
import org.openstack.atlas.docs.loadbalancers.api.v1.Cluster;
import org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancer;
import org.openstack.atlas.docs.loadbalancers.api.v1.VirtualIps;
import org.openstack.atlas.docs.loadbalancers.api.v1.Nodes;
import org.openstack.atlas.docs.loadbalancers.api.v1.ConnectionLogging;
import org.openstack.atlas.docs.loadbalancers.api.v1.SessionPersistence;
import org.openstack.atlas.docs.loadbalancers.api.v1.HealthMonitor;
import org.openstack.atlas.docs.loadbalancers.api.v1.Node;
import org.openstack.atlas.docs.loadbalancers.api.v1.ConnectionThrottle;
import org.openstack.atlas.docs.loadbalancers.api.v1.HealthMonitorType;
import org.openstack.atlas.docs.loadbalancers.api.v1.IpVersion;
import org.openstack.atlas.docs.loadbalancers.api.v1.LoadBalancerUsageRecord;
import org.openstack.atlas.docs.loadbalancers.api.v1.NetworkItemType;
import org.openstack.atlas.docs.loadbalancers.api.v1.NodeCondition;
import org.openstack.atlas.docs.loadbalancers.api.v1.NodeStatus;
import org.openstack.atlas.docs.loadbalancers.api.v1.PersistenceType;
import org.openstack.atlas.docs.loadbalancers.api.v1.Updated;
import org.openstack.atlas.docs.loadbalancers.api.v1.VirtualIp;
import org.openstack.atlas.docs.loadbalancers.api.v1.VipType;
import org.openstack.atlas.api.resources.providers.CommonDependencyProvider;
import java.util.List;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Response;
import org.openstack.atlas.api.helpers.ConfigurationHelper;
import org.openstack.atlas.docs.loadbalancers.api.v1.CertificateMapping;
import org.openstack.atlas.docs.loadbalancers.api.v1.CertificateMappings;
import org.openstack.atlas.docs.loadbalancers.api.v1.Errorpage;
import org.openstack.atlas.docs.loadbalancers.api.v1.SecurityProtocol;
import org.openstack.atlas.docs.loadbalancers.api.v1.SecurityProtocolName;
import org.openstack.atlas.docs.loadbalancers.api.v1.SecurityProtocolStatus;
import org.openstack.atlas.docs.loadbalancers.api.v1.SslTermination;
import org.openstack.atlas.util.ca.CertUtils;
import org.openstack.atlas.util.ca.CsrUtils;
import org.openstack.atlas.util.ca.PemUtils;
import org.openstack.atlas.util.ca.RSAKeyUtils;
import org.openstack.atlas.util.ca.exceptions.RsaException;
import org.openstack.atlas.util.ca.primitives.RsaConst;
import org.openstack.atlas.util.ca.util.X509ReaderWriter;
import org.w3.atom.Link;

public class StubResource extends CommonDependencyProvider {

    public static int certMapHostNameCount = 0;
    public static int keyGenCount = 0;
    public static final long millisPerDay = 24L * 60L * 60L * 1000L;
    public static final Object isKeyBuildingLock = new Object();
    public static final Object lastKeyReadLock = new Object();
    public static final Object exampleKeyLock = new Object();
    public static boolean isKeyBuilding = false;
    public static final int keyCacheSecs = 15;
    public static long lastKeyRead = 0; // The begining of time
    private static final long cacheTimeMillis;
    public static String exampleKey;
    public static final String[] sniCrts;
    public static final String[] sniKeys;
    public static final String[] sniHosts;
    public static final String key;
    public static final String crt;
    public static final String imd;

    static {
        cacheTimeMillis = keyCacheSecs * 1000L;
        sniHosts = new String[]{"www.testhost.org",
                    "www.snitest.org", "www.someothersnihost.org"};

        sniKeys = new String[]{"-----BEGIN RSA PRIVATE KEY-----\n"
                    + "MIIJKAIBAAKCAgEAvSw2gn5oWzlIQSwiN4JEjJl1gHtI3UItsIoTGZCbFZ292O4j\n"
                    + "/yR3AWo0FxbOUBhkZnrZG4hVm6bqQc0lwUx7K5wAQ5+GCVFrDst+qdxqb6I9QwEU\n"
                    + "2QIGZL8c2kvWmJHe0rt30x2I9Iam+t/l5SnVYgRjHC6ODbovxMUxC0Q5Hy84OHWF\n"
                    + "SnAEmkN2DlaOM8794Tq+DZnpmitZpcQRJXWIZpriQTLYYmO42AeGGNirUFLg4kkb\n"
                    + "zrt/ob9gmhtUB9/niZ0E2VRqk4vNMNshyOiqceg2e1LI2Dxexyhb0PBkp4IgH/HC\n"
                    + "9aovbW7X1rFAAsazTYYyL+ia9NfAv95PbUUK8psfvmT7BaAyYSU+/882QgMZNewi\n"
                    + "lZrFhnUh4a9NodfedYVePJeXVJV9ffnv3xT3JeYBi2FGM3XIQOFjANtnBQpMBt+V\n"
                    + "ZyeOJRBCtywCRmRNXCA3kf2GH+RMRKBBks2NyEe0iXJ4KbQW00sDTEK1k3X+pyKf\n"
                    + "LO+qZyWNyyfqqOZerBmhE/2xyzgVodO9IOZe7GlQrX3BzxID+cZwxgLaYc17GaX0\n"
                    + "VhkAG10MXiGFlYTcIBf9yfXswxes/zgDyuxJFJxCvhzd7roklkoNE7YLGnkORZTe\n"
                    + "aKTGpyuF8UQ/SCLfs6/r7oRw6W1PycwWKsG3mExyr8SfAP4C089xYDwzrh0CAwEA\n"
                    + "AQKCAgA10xmjYYIhypeja4JE1vzYIGXNa8acpdLIM1Eh6gImUV6OFZwKYX622Vf+\n"
                    + "5c9H/Ni8S+HwModhJUnr9Qn0COXWz6lHlGEXz6GtcPsWK4/hOYFCylD7OddF3RbB\n"
                    + "ieiCOqbywMJkQDgSkyl5ojEQVinPnL3ya9ChWUN0MkM1GHFETTQq5E1Lk7nLhrxV\n"
                    + "cg098nxMFGG1DwDni8QTJUlXpixsPU95C5WD0Snr1aAy+5sq3Jn96J1P4gt7oy9C\n"
                    + "SdpL70o63oFdsOYuGw4EA6WGO6Ubid3OkwxwciTpd7NALNYGz0CCtlAphAXAllQ1\n"
                    + "UvOKlqy3tyIafY1xct31+bQRvB5T0Z3ZGRVLqqQ1Uyyxyo3OQpWaYfZ0WD7dwSrV\n"
                    + "nDJOiK60YBzacLbFxc2j7LndBYTT28QahTVC/yzIzVi5jOpBsM8Z7zZCzXCMFsk4\n"
                    + "tIG7prIvhDTC9tBSIO2jOdarF75UHck/7P0cwdC7t3suun2cHJH+qHj71712HlbX\n"
                    + "hRQ1LAHsrZIHi1ih0GaG8U1LnlPVb0PNWCJ9nwkXoAqsgBjAktif4Iz/YXmqggHj\n"
                    + "oRMdYyHgzleX91/LvDtXNL3vd3f1derJ4qcLEVqJ18PPMxMyHVqDrrNGISNSBuz9\n"
                    + "87c1ha55TQn6A82A8QQYtgxtAy/FDk74maoQYBJEjaXZi6kQ4QKCAQEA6QufLd3o\n"
                    + "dBgHIccKY1w1eYzlIgd65uF78XR1xLcPkXlvcgc3o/1auFulAIalfBLUYR1YwgC/\n"
                    + "pHG28O3rK4P0LB0f5RpIbhIiUmWLtuIMH8LkEu5YLX5rlRWtwwAgzDcqNhLBzqZg\n"
                    + "izzSroX3b9nwgI2PZORS7k7341WGTvFbCWEDGj+qwD8dXwh3alC64xMuUGry3liV\n"
                    + "nJNIuk8fQNUH4oe7U72luU7RnQCLEDCGLxsjabsH8pi7FIQ2DHSMm7fcp47voIIf\n"
                    + "+3oaD9AGFV+hCCLhsGrO6ONYWVPkNpsEhDZjCTFN08IDas6R4RsMYyQZX7+oZZHR\n"
                    + "GRWSHypvF7hvFQKCAQEAz85QrlCntb/FEKlh9RcB1lW7A8hX2suQDa85GxfNxokW\n"
                    + "nwbdAcC9RVrveHy8rJE2j7lRpRTglamEBTjDEofbhF9hyqIxHIhU9WR5PBAtMSv/\n"
                    + "njBt60cav/wBseNSbGcwoqvRy8GByOiIqQZ6ioC3bQSJUArGqlAOL3eE67QfHShf\n"
                    + "5h+mGCwHW941aS/QiheeJit59g4xROm6mctYVCVwmmpDYgfm/ncCgKCtNE2LP02/\n"
                    + "rkvloDW+ghuntklf/FztPucgmuf0WypWok6j53MoDK1gf5nL1pWLxfoGkEhAM+ug\n"
                    + "CaCK1kd0swPIV6RhKkXJqbBd3g+PxiNaX8ChhcFE6QKCAQEAtolw3BdVzSFI7M8f\n"
                    + "tuXxyMxwaTActx1k3KKQ4E1W92Z7wYAft+Zpmb/GxVux4TZKferjq8m0UlTzq1Fg\n"
                    + "A8yug6MoaWYl13mCwrFhNxVl07buuFlSXggIslSxruy5w9ttx773aSrusgEpMKQk\n"
                    + "RMsbU0YOTEN/JcRa7AkAU3M/EGCZcoTt9Pk6w0Y5at/jEpviigDLTRdr0KGYtL3m\n"
                    + "x/xU2b3hH92LIlO3YdecFUk8wUbrfRSPTBkPaCmKwETGIe5o+iG+ViW2cV6ZbUtO\n"
                    + "iI1H77yKtIlkmgwBFq7cMV1Is1p6RigFQtlTneRBZWWvVTRgX5wahbwqYRCBTMIL\n"
                    + "FDRVZQKCAQASguBxBsJJ0HFvkIcnXAtP2oc0SQ56cRO+BFPj8/TisKeRNv1NAKxb\n"
                    + "l0QpyYLNJIplty5stDVlX5phFLNb3TpSymRdtqEFV6epzG94SECeVrsIjmvcP5NE\n"
                    + "cuB7xjI7cd3FP+UKb4xuJzi1Rdx4pgqw7WyF8s/LsCHzXIQ8sujqogQY7czP7mwh\n"
                    + "ZOQBIL9E7eGqqMjOgGknKGL0/EY8CLHFL54dB3MuS8+vQaDsPdQfqX9fuWcVJHR6\n"
                    + "BpikBllEk8qWheRDqFH+JQFep/61yW5R4sfr1flwfhXbZvBavfTqKx0wflqfx/G/\n"
                    + "WKDqHbyCFYFnWkyiUWSPU+rjCLfawGKxAoIBAHQTEOwt4Edc8k/Gigl58vDqUnkR\n"
                    + "cjkwzdBQSu6qEmd0OuWojKvywOZRV32XpphbzjWUPwAvCAfYFKqkq+PtBerRcN/l\n"
                    + "R/wQfvO7lnW0IhwIkipqtgFB3zA0/QUyFTjFifseM0qGdqU/qtDQdzWpXfzOQB4q\n"
                    + "0ieLaXPJfELEfnU4xAj91kGlIYXkITL9Gt7l7l+ODZNnyo6xnKrKFzYU8Tk6SgiW\n"
                    + "lSCiImMZ0b1F8xuzWB2WHM3Gr7OxJo+pt3/3AzJzv2Ro+jEgGm45DUAqVyLuJgtJ\n"
                    + "eXwbimG8H+7ag9AmR1gYDdhFBjyQ2ewOZmntomL2226grDl+bh/Y1UTmUtQ=\n"
                    + "-----END RSA PRIVATE KEY-----\n",
                    "-----BEGIN RSA PRIVATE KEY-----\n"
                    + "MIIJKQIBAAKCAgEAykrHpO+zVER8CA/bWoBqYIMhvcCtmOR2L56TzaukGySgsY5r\n"
                    + "e6RslX9T6sd9wJUsmcWbjysVtOMDonCp2L56SUoLjXeAMlsFTw41G86ApzXxnUg0\n"
                    + "aTnzMmzFvrW6h9CsWmAq77NOQPV8EK7wSjCK8JkaEQU6kHJU4diKEoVp4hglbZxn\n"
                    + "TpN+5d96MWAprJ98lUqMk9dVNt5TnLZW/BLuACqYxv5qUAwePD6ocJ6oUuvRsLXY\n"
                    + "Y/XED4QMEN7qFz7+BozCeGIbtUaKOrwm/EFP8m2POzPYJRHsRIVlYE5Cv5aZq4We\n"
                    + "j1Xu8QRdD9tpW4GYoDdAu4odSUuI3C1cIav0k9B8vYkRhEE0A2F5/SSZGF0m5wgh\n"
                    + "q6zRpncEDY0PTtnmeJgnsrwEdwrG5R7z4eHP4VPzFIH2WwtGNtI18XlZKS7WM2fU\n"
                    + "Nz49tj8Nb5ZskkjkD++DjnTiITJv0/6G2XJh0sy4OFojjcSnG5tADQTdBds2WRMN\n"
                    + "qrAabuZK3M1s+Xlvul2VnRGEkKPGhp/QPObclg1Csy37k8m3y1lvH4tnpujnxLMM\n"
                    + "W88Ue0D26U3n4qnJSCvOPa5zOjSCAcbLqWCyPIv6SFsSfwgnL4b4GuClRZEoJeZj\n"
                    + "keFDBwpqhc/ppXycl+sBqid+h7fNfkyT420HLH2dL1RK97Nv+pKsFZIdtCMCAwEA\n"
                    + "AQKCAgEAnz36pwXiPyWiFZ6V6EpKcysG9p6kiUTfHGFSrIa/KnbM4LjiN3SZ3Z1b\n"
                    + "6+eaQG2trGb4K92Yusox5D+YFHztOqBFM9msWvd32GGwwPDwKbC3QD7Es0jg/wLN\n"
                    + "L3FTIvLLmmE1q3eU1GHqhh+QHHbWCMwczuU00tyNoTOajTXkwgyL+hsy35IQRNiF\n"
                    + "EjVG2RK7ObJzszXBaO3fF/AZzVkBbbQajBteiSDg9D/GzNC4ZH4qlVawvw+KhURH\n"
                    + "U0iHMkFuKq3HsIhak3qx+cZ/Uff//TDtnYcUUSoouU/hG8y/kp303ibTSLY24Be1\n"
                    + "QnRQHa6I/SZBmOACad5CIEC80+zPG951FTSbdNTefQ0S0bBuVSHFRBPGSCOjKRf7\n"
                    + "+CIXuL55UdoF9KDeN3bgKfHHlnTmmgOOTbC97rCmJm6v1Ot2W1+LpSHRYm9zNo67\n"
                    + "IZMr6J6PJ5B1i19uwi75Wgc10C6IeN75pB7jOsgVv0lIIYPI1tpkkMKd7lZU/7kx\n"
                    + "uYILq3nHEjtrlFboquvmQHeVPm6g+5IDOWyR0yS7ZL8MxMsqdfBeA+1vZPDxEBok\n"
                    + "2t6wpO+djvo2q3GUaf0aQ9RfejqFMHu6A5ED/q1ofDxBgGpsPVxnIosdKPv4F5xO\n"
                    + "0TSTsDfKBSw1kbnxObIAHKk6kGekg8IPKEapsStV/ACob3g7YMECggEBAOunNe6a\n"
                    + "dzrCLcAYfUeNe3mrvMb5jnBzgrC8EeTntOJm2oWQEZmWapJyDHENeS0NGsBpQtEu\n"
                    + "lHuetFlW59yijC+ECvhpg1ygR22864lFl0AYjlNmfssAs95LwUmmGz0Pi2HxZOLA\n"
                    + "/ngkGL08cxqFOfio6cVmzPz9dL4g7hslWQWos8tFbehby5hLzThVS19C5KqfWRfw\n"
                    + "xSQFYvk/z7556i4lu7miRHwhy6mxZoRBvYERLya7kiJIPj+Xx7upNDjfSnkreJKx\n"
                    + "8xdvd54fXJ5a8Rl3SYz+x7XaJCKqwKBpagGNTBGqR2JRZgZF1xIdmV2QRrDpUkU8\n"
                    + "dq2T1SIA7XYlzJECggEBANvCKzqDOxzMPH01ASwX14V+uopOxSrD67nKf4/SHk7I\n"
                    + "h8usaGwf0qo2nNNTyOGN6e0f1xMGH6E57c9zT2Jchnq3RjsfYAu0w9673diLqisB\n"
                    + "Z7WDtcsTudFAEJmMAGR1hgNXjFXkszqGr7SfTR7rslfmydPvfPIB5zNIKJ3KO/r3\n"
                    + "B6uzZlF1DzeOyiCuVPuW+4kXWeN9tBY3lfxv4XONnSfoIiUxv5cytoWmpVtNTcIt\n"
                    + "gCAmASoEKX4E8tKjbqUphWgYgPzelo1tms66F+L5Hp/cxYMMQ3hO/uPd1WzFDdxe\n"
                    + "CqoT4V5LklHHu1kxHjafH84OxPaX2UFzRLf5JuUpX3MCggEAd172dO2OJBek8T/2\n"
                    + "hj5nI9UZBXn464G33CsIrXmqKV2MqEe4gqKtTY20O7hlf6q5uGoZzLjZwl08Yl0h\n"
                    + "iqRUeuVaYUehoVQXmsEuVxUHbxKBVkH/THoIozygptwbxCyRoIWVYz/8J4OxfHqY\n"
                    + "nACMwyddc/+FK0qAzqcDtoCOvc0ddoSvDhsh4P3U8QpT6eeKydt6WCRA2ORZpITs\n"
                    + "iUJcnA/FLl3Cb1b3Qfh5Q9nBUxYPWoF9nxwVXEVYiflGWODTatnYdAbsaxyt7U1Z\n"
                    + "r2XF5+6DNYcCR14/G8h/V0LOzgZzWCd3jZhKJDdQoP6ZZ8nj5U3rvOLKA1d1iYoK\n"
                    + "v+SZ4QKCAQEA2paUsSr/jXGJaXFjrRw6AEtB+XjInfUrcC6AG+qAwxSsHmdrpsEw\n"
                    + "pqaT75CTa+fhG0BkikO5CoG4jnTALbKjAUVz0jMvMfWoLtDnphGmPgizw9Uy6sDA\n"
                    + "P+ddm52qdkJ//qsCqB1IOZrnAt7Bs1P2nYCumOTKf15tPpEz9V/HsaYuNxfHWpb5\n"
                    + "f+XEICXH2kkgAgj8HFBY5RB9XA9dTbc67wrzi9OkIx0KeH5+VAbcpZzkqRWkasqh\n"
                    + "CvyZG/P8hsR6MkTNaLlvP1Yaei6Yu+fYEmNjAysboXvSBY4OUJbhZqUULoB/12+M\n"
                    + "J1RoghtdVi+JT0lZmX8JVxT08ZoemitBCQKCAQAGySHHMAhXQsN/n02znXXVwT3b\n"
                    + "rjKclhN1kVAq0wgZv0iqPIEF444q/ZID3dj78vrnnolRhXvMHs3rUz49onxuu33G\n"
                    + "WEPBsn/Xta9QstL8Y9Fykv2vqduOtr9ZuesofLSVb2vHR7/IMtUK1Dffr8VMfZvI\n"
                    + "RsR3aDgRlVJGhF5je+guV2OugCHCVmxUwdcJgSUS8qajMLuk3mdMxX0wRu3sai5z\n"
                    + "G4NNPac/6wAgLifEp0Y/iBRXiDckQAfHJLmm3iaZ/wPeOCQt2L1EK6z4Ra4PL/9/\n"
                    + "9/D1IHisW1oKEw/z1s4eBIJDCdg9vOD8nTQLIXYz0HKFvmZ4LgrkRJC88myy\n"
                    + "-----END RSA PRIVATE KEY-----\n",
                    "-----BEGIN RSA PRIVATE KEY-----\n"
                    + "MIIJKAIBAAKCAgEAv00JA2n6tn2aaPkw2GYzEXCBUzxwR7r80U+NFaDngCpDvtqd\n"
                    + "oXo9iu2V6I2aMxvGKsdmtV/mo1quvwSHlJICoIUWHdibf8ItLdT3Pd4jjr80Tez+\n"
                    + "exNusbqxjW+ZsUfEB8wlOXAe8b5rgybxwrg9kw7VhJMCWx/KRuorGJc1Eqj0oZOu\n"
                    + "0TMiWVTWjmxejsDLwJgRmuk5ETPippQ2cP4FS0qHeJOZ4KLEXX7bao2vUTiato8B\n"
                    + "IS/2JRbauiekR+ouoijUeoRTXRO031vZ7GhxADdvztJXSn89A34NFc3enn8ZJeVQ\n"
                    + "r47ZC2hILN/ZOGSiACoPNSVYOtGUg8HT9IPki6h2mkouHSxYRjGqxJrxz+U0XvVi\n"
                    + "9nFQWjwQlAwZ9fw9m4TRsFHnbEUMEtneGtEiqURCUPZaRcLGxvAdalKMDDCdKG03\n"
                    + "QieNO+vAcg+p/o3qRRtfQ/H7kvDqEBEDMUIEtP1/zx+Nxi44snXvIKVSr6RHsRrW\n"
                    + "zpiLy0vp7Oqh75EYZsy7Sot9v/3iNgXZhlmC2zGyM2o7fWBGIV6L9G9mvo+qsO8/\n"
                    + "PCyHWU/fBKTGYoW+/pkm+ah2aMNVY+OrDKagE0LKFRRk+zmJF6qeFQUm0fq0xUmD\n"
                    + "Uq3JGVHKk3/9wy6h3UxvFOGUzHcoQMZui8lQHCK6zR6eJKo7Osj4Xbyz1McCAwEA\n"
                    + "AQKCAgBBCOdvia/6Kmk8FrtMVjI2U5EjwwBI4oDgh7LnD3SkVboCQEAmjXAusuar\n"
                    + "0LgDeAW+tVttfdTi5AlUVy3gjGLoP1Ztfx4eKN1DH4uONN7RwJ7/fvpkzQ5szZNf\n"
                    + "CfmuEz/JhtWA2WUruhjC1ZKLKCeGvxxHndLHN9+EjEdqbL1WQqon5BUiecfLptPq\n"
                    + "R10+Gr0qFIinqXIFLyZ2ixyW/US/e+7iRwuhGrS3NFFTwCdB7YRrW+J+bde4dDSX\n"
                    + "5LIs4KhJ6KeTnzjYA8MRzCAiBr+j2zTfkxKp+Oe9MDUynHJ6X6sk/viXNnEDN8iR\n"
                    + "uOgYC94nQCU7ZB7CoE1lHZf5IH2phe+RyeBYnVjoIZGh1qwLpCls1/c74u1W535z\n"
                    + "Oi5CFCsmgP6d48HoB8etgh/rCHnzj9AAaTrx+8SHUGp3vFcOlE/h+yDFbtF7U+hJ\n"
                    + "4tMCWU0vZ7zpRoK4Lu4NiiFEBYKa5lxtFmzupH9wVDZKc2VF5gSJp+PovII/P+xt\n"
                    + "FHeUwZOvAmWOueMhtH3y98zXKMmdPn4Jn4TSXj1uv918r+KLKAtVpvFKu6QEROHc\n"
                    + "1JoRpQB6zrp+8cuPCN+NNiUgTuGVSWt2yB/jDzwYpB/aQbgZS1g3hB7wgafl7VyG\n"
                    + "QkGpLxRvnJ6UKcwAQjrc3xx//wlWODXFXDN1MBvF/2lLOnvBAQKCAQEA8QoC9OCx\n"
                    + "ehXq5ocq6Nuv2GqkwZMRkwZJ5AdpHP2eHDrYRZDFcuC/lS/yi2z+Dnf78Qb4HTD1\n"
                    + "kkkgyp7p5XMnZJR8uwMwaWd//w9vdSC5nrLz4t8GzON0idlz9HKltHgTcV1FvILL\n"
                    + "6qWQpFnMg7LWwqFhTXdOZ3WdNcvTUR6Iwzaypkgui9ZStKh7z73Mm9E/wHMvisc8\n"
                    + "rELdgoqZc1cm1GeWJDQc8Us9ztMQnjsL86WAzJK4uigETgKuUDjPraoW5mycsnCM\n"
                    + "z+tUOwKwQGUhrJSRZTNQeGPuAgsV4nvDqYdhu6vdiFskKUQnD5PIIvCYC276sy5T\n"
                    + "Zfb5+FV9zLBRMQKCAQEAyyy1jwEY/nG5k3XsJTIicAEk6B0/SzOdNAh+NQuoGB1M\n"
                    + "EeCwYdT+YqZKqUoUHYTxaxcgq582AbhZ4xtRMUVCDuDL7QBPs8XV990/4RNGcpOZ\n"
                    + "C96hTKqQXZ4GXsLwZ4KD2IdtImYyaUjN8d0K2dZXazo3ZG1fpZuytBZpvpiHg8nM\n"
                    + "KePiL8ydFttUqh1kX6ZS0tQcE3/QKBx8a/8wXQvhg6rRafyr0FhUBnIOys7rldgL\n"
                    + "u/jvEmcEiKSKfdFk6X8Vgz00ItIEKXcQ7D8IGBoVAtM7YSi0h0rK6vlzhJkfaHOR\n"
                    + "tND+0BPDKl+nEOvfL4EBuq+Vi5mIm6wlS0RZgXHHdwKCAQB8WK7Xx0andcEdteNk\n"
                    + "B1k66yEdn5I0Z80crtoCE7BKXe0OTjYeImWTQA3+8XZK3okBeZfr70LweXtKLBr7\n"
                    + "vKE42QVTsLx70+ILIdpc8Unw3d7FTksCzeBj8ONJ2w8y/7MAC58bNdqerVTmBFFm\n"
                    + "K49xDrHUtdSjuOAmTYUxVvA0zPXypFAm4aT+/+AHdqGkTJl33bRMAqaBM2l0m623\n"
                    + "++DTOLES8jNwI2G6XdY32rw6sEm2v4Y+usAlwmpMhlJVA+cWbjeUMk3iQUPNwYBN\n"
                    + "aB6sGmS7cXJ0+RBiy3c5xKFWxrmbiHkbi/EmvLrt5U4dAENfupvobeCmPHgOLYRd\n"
                    + "YSQxAoIBAQC+UX0pHfAZuNILLbUtCo9zt76op2z4trqVWTdP3UFyhU8SmBThL7Gz\n"
                    + "Gu5pMfkxxayXkiyKVzMx3UxmDtq5epTsdurPcTE3xyQtcNU7suCUx2nf0oXp2QQu\n"
                    + "ja6N6bFkZL+4fvNd+7lRIl+4JkTZ1YxB52Cn9ERaT0TQqQYZAaw/aTKUMuFXwSWN\n"
                    + "mZrvV2UErDp9Py+lnF6SlEyApaTaJBz1R3eu/n67RWJAP2tX0wifbqdfR0o7RMDc\n"
                    + "drU8Mn+CsgamzxgdXfmEFgKGKm0clqID6XaABq9E5Gy8OztYhE8FeuyMIhHvO/Pf\n"
                    + "p4ZJtYl1aDUEeSFuiT2MBW9R2A630Kr1AoIBACQ+6cogQweEoaI7+DkgII/t9Ya+\n"
                    + "1J7MJqJXqvD9H8bzEUVvKrAUNDVY4aITPeitzBhiMAPYTR4pFzimgiqVnMp7OEoL\n"
                    + "rD+2YiDcajyEq4kxGKIMi0bzjoGy1Ki+qLENpkkSCidkvUkQTqqe4MhHFVSJzH9e\n"
                    + "6vVaiwrl59iYwv6LsuvMZkUZYnqm4sGnThAO/GAxSs8YaOjvI1B5l8k3E11DTjT1\n"
                    + "f4x/hd/gKCQNqg35XTH2QmXszA3lFdc9cJllB9KZEZaY+zv3ABOmUAiiWBXVr3xh\n"
                    + "n1mO6gF+NyK25wXdaNhelZRHwHtfkIUcHDWs1L0A0CbIU+En8jG4+uIdfvA=\n"
                    + "-----END RSA PRIVATE KEY-----\n"
                };

        sniCrts = new String[]{"-----BEGIN CERTIFICATE-----\n"
                    + "MIIFtzCCA5+gAwIBAgIBATANBgkqhkiG9w0BAQsFADCBhDEZMBcGA1UEAxMQd3d3\n"
                    + "LnRlc3Rob3N0Lm9yZzEcMBoGA1UECxMTQ2xvdWQgTG9hZEJhbGFuY2VyczEZMBcG\n"
                    + "A1UEChMQUmFja3NwYWNlIEhvc3RpbjEUMBIGA1UEBxMLU2FuIEFudG9uaW8xCzAJ\n"
                    + "BgNVBAgTAlRYMQswCQYDVQQGEwJVUzAeFw0xNjA2MjIxOTI4MDNaFw0yNzA5MDkx\n"
                    + "OTI4MDNaMIGEMRkwFwYDVQQDExB3d3cudGVzdGhvc3Qub3JnMRwwGgYDVQQLExND\n"
                    + "bG91ZCBMb2FkQmFsYW5jZXJzMRkwFwYDVQQKExBSYWNrc3BhY2UgSG9zdGluMRQw\n"
                    + "EgYDVQQHEwtTYW4gQW50b25pbzELMAkGA1UECBMCVFgxCzAJBgNVBAYTAlVTMIIC\n"
                    + "IjANBgkqhkiG9w0BAQEFAAOCAg8AMIICCgKCAgEAvSw2gn5oWzlIQSwiN4JEjJl1\n"
                    + "gHtI3UItsIoTGZCbFZ292O4j/yR3AWo0FxbOUBhkZnrZG4hVm6bqQc0lwUx7K5wA\n"
                    + "Q5+GCVFrDst+qdxqb6I9QwEU2QIGZL8c2kvWmJHe0rt30x2I9Iam+t/l5SnVYgRj\n"
                    + "HC6ODbovxMUxC0Q5Hy84OHWFSnAEmkN2DlaOM8794Tq+DZnpmitZpcQRJXWIZpri\n"
                    + "QTLYYmO42AeGGNirUFLg4kkbzrt/ob9gmhtUB9/niZ0E2VRqk4vNMNshyOiqceg2\n"
                    + "e1LI2Dxexyhb0PBkp4IgH/HC9aovbW7X1rFAAsazTYYyL+ia9NfAv95PbUUK8psf\n"
                    + "vmT7BaAyYSU+/882QgMZNewilZrFhnUh4a9NodfedYVePJeXVJV9ffnv3xT3JeYB\n"
                    + "i2FGM3XIQOFjANtnBQpMBt+VZyeOJRBCtywCRmRNXCA3kf2GH+RMRKBBks2NyEe0\n"
                    + "iXJ4KbQW00sDTEK1k3X+pyKfLO+qZyWNyyfqqOZerBmhE/2xyzgVodO9IOZe7GlQ\n"
                    + "rX3BzxID+cZwxgLaYc17GaX0VhkAG10MXiGFlYTcIBf9yfXswxes/zgDyuxJFJxC\n"
                    + "vhzd7roklkoNE7YLGnkORZTeaKTGpyuF8UQ/SCLfs6/r7oRw6W1PycwWKsG3mExy\n"
                    + "r8SfAP4C089xYDwzrh0CAwEAAaMyMDAwDwYDVR0TAQH/BAUwAwEB/zAdBgNVHQ4E\n"
                    + "FgQUkj5Np6kN8oESWP1m0nBnzYCjzW0wDQYJKoZIhvcNAQELBQADggIBAJ9qCTLm\n"
                    + "R3dGf8bgRxk+38w0suVwqjUcHN48ukPWqMEuyIZadsx35aZ3Ob+jJbvZQIPRcxJE\n"
                    + "U0KZkl7H4XsLK+BuNruIKp7ng9oDwxAzBS1IyjAfhsboyb1p7GkRCHr0Oe2/H8MB\n"
                    + "WhDahZz5zH46tMRC+BD1WOsC8BspTDHBZbtx+X66k5PtwYqrjGj4XJ8P/PbdsKFw\n"
                    + "QOyYZlT2M+pdVpHkoh1h6G7qL/OEk7DdrkOi9E3yEYj39DqfKwEA6qGsEiCKfPz3\n"
                    + "2XXQQ8HnF29orVtmObRAscl9SMtyhnfjpuVoNv6y/rDyk5mogI+C0ZU85zA4/YFK\n"
                    + "7wBEpNU5xC4r7LhqBkifF25XRbXRRCvVStpNEDX0mX8rmgjV/o2kKKQ47xl/vIKw\n"
                    + "CTkYOaivYuxPzaggPf+P1xIK6Ef7zF8Elo/hLXippd4pF+6Dvr4LZ2W6VCYsROlX\n"
                    + "jSwFHnxG3vzrl0tm4GAdlJk4K3YfN+VIXqXhZ0r6kkjyQ8WfpZfME5m70RNAQMup\n"
                    + "hyMQtVh9dPmFB7M13WINX9YJ50Y5wu4nkd2ZRZbbZT4FfDVbd8b6BuKmOVZrEHWK\n"
                    + "Sr0mPh9LcZ9zh33m+JekObC0frs5mkvU1OyNMZqWyy/bnUtgIiYQp0o3plOKVj5b\n"
                    + "OZ4o1OJ/9YWu/SRSWW0+nD+3n7erYv0pMv0I\n"
                    + "-----END CERTIFICATE-----\n",
                    "-----BEGIN CERTIFICATE-----\n"
                    + "MIIFtTCCA52gAwIBAgIBATANBgkqhkiG9w0BAQsFADCBgzEYMBYGA1UEAxMPd3d3\n"
                    + "LnNuaXRlc3Qub3JnMRwwGgYDVQQLExNDbG91ZCBMb2FkQmFsYW5jZXJzMRkwFwYD\n"
                    + "VQQKExBSYWNrc3BhY2UgSG9zdGluMRQwEgYDVQQHEwtTYW4gQW50b25pbzELMAkG\n"
                    + "A1UECBMCVFgxCzAJBgNVBAYTAlVTMB4XDTE2MDYyMjE5MjkwOFoXDTI3MDkwOTE5\n"
                    + "MjkwOFowgYMxGDAWBgNVBAMTD3d3dy5zbml0ZXN0Lm9yZzEcMBoGA1UECxMTQ2xv\n"
                    + "dWQgTG9hZEJhbGFuY2VyczEZMBcGA1UEChMQUmFja3NwYWNlIEhvc3RpbjEUMBIG\n"
                    + "A1UEBxMLU2FuIEFudG9uaW8xCzAJBgNVBAgTAlRYMQswCQYDVQQGEwJVUzCCAiIw\n"
                    + "DQYJKoZIhvcNAQEBBQADggIPADCCAgoCggIBAMpKx6Tvs1REfAgP21qAamCDIb3A\n"
                    + "rZjkdi+ek82rpBskoLGOa3ukbJV/U+rHfcCVLJnFm48rFbTjA6Jwqdi+eklKC413\n"
                    + "gDJbBU8ONRvOgKc18Z1INGk58zJsxb61uofQrFpgKu+zTkD1fBCu8EowivCZGhEF\n"
                    + "OpByVOHYihKFaeIYJW2cZ06TfuXfejFgKayffJVKjJPXVTbeU5y2VvwS7gAqmMb+\n"
                    + "alAMHjw+qHCeqFLr0bC12GP1xA+EDBDe6hc+/gaMwnhiG7VGijq8JvxBT/Jtjzsz\n"
                    + "2CUR7ESFZWBOQr+WmauFno9V7vEEXQ/baVuBmKA3QLuKHUlLiNwtXCGr9JPQfL2J\n"
                    + "EYRBNANhef0kmRhdJucIIaus0aZ3BA2ND07Z5niYJ7K8BHcKxuUe8+Hhz+FT8xSB\n"
                    + "9lsLRjbSNfF5WSku1jNn1Dc+PbY/DW+WbJJI5A/vg4504iEyb9P+htlyYdLMuDha\n"
                    + "I43EpxubQA0E3QXbNlkTDaqwGm7mStzNbPl5b7pdlZ0RhJCjxoaf0Dzm3JYNQrMt\n"
                    + "+5PJt8tZbx+LZ6bo58SzDFvPFHtA9ulN5+KpyUgrzj2uczo0ggHGy6lgsjyL+khb\n"
                    + "En8IJy+G+BrgpUWRKCXmY5HhQwcKaoXP6aV8nJfrAaonfoe3zX5Mk+NtByx9nS9U\n"
                    + "Svezb/qSrBWSHbQjAgMBAAGjMjAwMA8GA1UdEwEB/wQFMAMBAf8wHQYDVR0OBBYE\n"
                    + "FI88abhb+9QlNN8cdKNeXg5vcSYQMA0GCSqGSIb3DQEBCwUAA4ICAQAFUWF4z0T+\n"
                    + "PbSrYaaYZwY9DwaG7rGCx6SgeuYG5edvoI+1B5hESu6E23+q6SKQ7Sv9l5wnr6Ma\n"
                    + "ANZz/1+R5fN9cL9ljbIFpy3+4yWZ5kLutwErnF/Kn2JxER4Z//RowH222OV66J/5\n"
                    + "HwwzFuNLFed5xIxBBziDt7hh9y05iUL811NkI53IkddLR1NOZy/40MzDhqZk3S1i\n"
                    + "ifBz+OC1pOCsz7zlUf7UL027pzKceY2wS5ME5fqTvR0+cVtblG9ZfeaaHmC/O1Jr\n"
                    + "ezPXHAgqH4FHrJlwIOeVtsHPx08kHvDmvZwv/+I8jPiWWK6A0ETCHlmV6MLrmhEa\n"
                    + "KnYLnNuuXxOGSQ/912TwW4nRB8VfFt4hNIpnn3bBdi8zU0DHCB2icoZE3vyYJqjQ\n"
                    + "brMan2j68mkAlu04lDfhUBfM75SQ9Zdwq6HnLK67fs3HsI/Bh8DTA87ZNXeu4eTv\n"
                    + "G0fv8PJdnOa73Ku3FbF4Mqt+5TSViD1Nx7RgsXp3rFEXNuaLQLxFKDSSSs+gZyka\n"
                    + "9bWmc61kc/sDwW3TMfXRBc8e3Ig7PbVutn74Xk2Mp7VqSxW9O2J2mN7xIPYofHtI\n"
                    + "ajoXrPbQY/XMX2H5sINWbPQGyP8uzNtf09Xx+v+Wa11VGrpksTT8prWL98EbLwUV\n"
                    + "oDQJOjZr5A6Woa1pXUzmBQ0byHyaDJfJIQ==\n"
                    + "-----END CERTIFICATE-----\n",
                    "-----BEGIN CERTIFICATE-----\n"
                    + "MIIFxzCCA6+gAwIBAgIBATANBgkqhkiG9w0BAQsFADCBjDEhMB8GA1UEAxMYd3d3\n"
                    + "LnNvbWVvdGhlcnNuaWhvc3Qub3JnMRwwGgYDVQQLExNDbG91ZCBMb2FkQmFsYW5j\n"
                    + "ZXJzMRkwFwYDVQQKExBSYWNrc3BhY2UgSG9zdGluMRQwEgYDVQQHEwtTYW4gQW50\n"
                    + "b25pbzELMAkGA1UECBMCVFgxCzAJBgNVBAYTAlVTMB4XDTE2MDYyMjIwMDUwOFoX\n"
                    + "DTI3MDkwOTIwMDUwOFowgYwxITAfBgNVBAMTGHd3dy5zb21lb3RoZXJzbmlob3N0\n"
                    + "Lm9yZzEcMBoGA1UECxMTQ2xvdWQgTG9hZEJhbGFuY2VyczEZMBcGA1UEChMQUmFj\n"
                    + "a3NwYWNlIEhvc3RpbjEUMBIGA1UEBxMLU2FuIEFudG9uaW8xCzAJBgNVBAgTAlRY\n"
                    + "MQswCQYDVQQGEwJVUzCCAiIwDQYJKoZIhvcNAQEBBQADggIPADCCAgoCggIBAL9N\n"
                    + "CQNp+rZ9mmj5MNhmMxFwgVM8cEe6/NFPjRWg54AqQ77anaF6PYrtleiNmjMbxirH\n"
                    + "ZrVf5qNarr8Eh5SSAqCFFh3Ym3/CLS3U9z3eI46/NE3s/nsTbrG6sY1vmbFHxAfM\n"
                    + "JTlwHvG+a4Mm8cK4PZMO1YSTAlsfykbqKxiXNRKo9KGTrtEzIllU1o5sXo7Ay8CY\n"
                    + "EZrpOREz4qaUNnD+BUtKh3iTmeCixF1+22qNr1E4mraPASEv9iUW2ronpEfqLqIo\n"
                    + "1HqEU10TtN9b2exocQA3b87SV0p/PQN+DRXN3p5/GSXlUK+O2QtoSCzf2ThkogAq\n"
                    + "DzUlWDrRlIPB0/SD5IuodppKLh0sWEYxqsSa8c/lNF71YvZxUFo8EJQMGfX8PZuE\n"
                    + "0bBR52xFDBLZ3hrRIqlEQlD2WkXCxsbwHWpSjAwwnShtN0InjTvrwHIPqf6N6kUb\n"
                    + "X0Px+5Lw6hARAzFCBLT9f88fjcYuOLJ17yClUq+kR7Ea1s6Yi8tL6ezqoe+RGGbM\n"
                    + "u0qLfb/94jYF2YZZgtsxsjNqO31gRiFei/RvZr6PqrDvPzwsh1lP3wSkxmKFvv6Z\n"
                    + "JvmodmjDVWPjqwymoBNCyhUUZPs5iReqnhUFJtH6tMVJg1KtyRlRypN//cMuod1M\n"
                    + "bxThlMx3KEDGbovJUBwius0eniSqOzrI+F28s9THAgMBAAGjMjAwMA8GA1UdEwEB\n"
                    + "/wQFMAMBAf8wHQYDVR0OBBYEFMgvtFjtssgt9Q1s4YUNtHFDplxVMA0GCSqGSIb3\n"
                    + "DQEBCwUAA4ICAQABntJICf/vZIXNfTT62F87Dsj7PoNLD2PlnU8vJNVUnYn/+VoT\n"
                    + "09yWZAL3TaXPLXMqLFNGOSsr7jJFg0UnMKRAK2cKfy4Qt6s+gVV2LGd8xd8btdWo\n"
                    + "xEVk+uLvLuHYA2+44TgbHmxT+KgY/AbMSQ19rusnOLkMUWDBUumLCB8fj6omUl5q\n"
                    + "HJnlfBnDufkeCy54hShP9IRegPbDBmN9O55Mj1CoAbeHIP48pbQANOwRWqVWJj6n\n"
                    + "aIYH+SseaL2tvfaESIrutNVWpycLrjy1nfKcr3OdzJbj43oXjsrt/JyM/Si6fiiV\n"
                    + "sH0TOp1OvXPJ/sy+hiaXUb6yGn3OoShNBHeLNEYJtcPbIWp/iQ3x8bCiAatuavyL\n"
                    + "VSDut59EwRvpkboLb8cAbAwEHeDTGX5cV2RV29ovtzAb5X90ps7Bq6ePSaNsFeCV\n"
                    + "AtJCrVgyGor18OoHGP3AE1F13MVLsEWudmEUYVcvJ3V+q5Tmc7acHuPpimGXhqVZ\n"
                    + "nsxIsSzvssKiaIXWidlzCDK/7KwXy1cnbtUe7f5KyTaW0PSwLHuyYbDa8vUDx7JA\n"
                    + "/GCpAuVZhFRqgY+Dzi3jw94wZMyPumiwT5ro8nJw0dH4SA7CLW5XwAEXTczQAq95\n"
                    + "1SWHjDGZkk6nZ95MjOxDGGI0DnlSKsN3joF9C+Z84i5UJ/HCYxboTAPA+w==\n"
                    + "-----END CERTIFICATE-----\n"
                };
        key = "-----BEGIN RSA PRIVATE KEY-----\n"
                + "MIIJKAIBAAKCAgEAnaf69IQZC4SBDfhwWz5svh6VHOhwaKXIUCBygKf8p8II7pIm\n"
                + "slkwH2CG0T/3fHtT9tfTb/7eANlBOQP5pAYdB8HgudjGCLnSXFjf4sFKJzFHgqOM\n"
                + "ABzMdZLDzSYn1pwR03eYx6aRYqBHdD/MIRTspdU7FLKkTDCkeE/qlbatdBZDVmyI\n"
                + "JjgcwPdPOnHbAN5XmDiELKWtpvkKghyFOaMCanJSGljHIN8ibOvYZUj/QKDaBQyn\n"
                + "1PGxXscIBYvnn5XCEtw5hJoHxTHX3jYNctAbE/251J0VOThK0oqW4zXG1pmivhwz\n"
                + "EoBAIfQ9dc9kxtsvz3TvBi/O84uuh4B2gLoE1AqKlJ2BI96hiUjXnU1mXovj9BcC\n"
                + "0cE9EZWqMsz20MhuvEmLLSANzmKJ07WOcvn+++m706huKndi6gT/2o10ipF9taY1\n"
                + "LQnwAENtTq6E1NTittEeAeoaNm4C9m8DMD8NpUEYnvaZwDZsWgcRUpmlMiwWE5Ru\n"
                + "GnfPzvQOjBxVAJnhkHEyS0hTOupi4c7EW6nc3X3oL0AmmDyZvNmyBDDpQMyDIGv2\n"
                + "l2+W9aj9Es0JeykTYk012z4hVab4/sUmMjviktRzYBgzaFcxBkW2NZtar7JUS2dn\n"
                + "1ejmloaBxHsNDRGWoCiAtgzJ7poUp+CUrrOkoETtmwMBlGT92dWrQA6GawcCAwEA\n"
                + "AQKCAgAEbvvksm5N350NeoYWWswOEKga1wKKPtdCQZdWvOKjCRbdNqj17QIob7t6\n"
                + "2PSpwIIc9/bPOHifx3xJES6NCUr5s98Q+uKezjL3O9yX8N2X+o/LQbQnMKgjSkxN\n"
                + "UZxfMaZirwNR4gJGpsE7qKuh5oe9JiDyNQ/fwKJva7fqG+gG0rV0EbtGb9+HIa1N\n"
                + "tHP3M0l9U2GMK+CVSH2eKRUqCMaBndNnQEXhS8UZEQzV1FaxR5S5/aAeoeleA/Ta\n"
                + "yxNpbnm1tBG+A+LiDcPHUPfR2b5ZMpJuQzicklOwVgtmOlXsJQfplrts8sRa8BZm\n"
                + "YL2xxeozSFOMdf245Z2z2835UsHd9Q32+fHBx/3Oo8ko4qHt7Zg1iuNa32OEwmBP\n"
                + "K3Wp5MGRa6aKpOuQXNP5fJZgpTMwNrBbnkwNXVlM//qFdOcdc/zcEkleHwh/RbDv\n"
                + "dfSzHpc/tvtFVDPnD8gOdnfnygN5tYwGu912JT6v7HkS5skUFi4+7aqNaNe+zJBh\n"
                + "ZFtS/c4pX2wVrcsGhiLSYMdJfceQf3AjvlQcoRSe+a9hCAtY1vUPqUsfJi+3Ddv8\n"
                + "YTzVUP1o3jn57WPswLo61WJa3NIYVAxRl/0/Tb7kfl2oNrv5VAwcBMrt55MuLdEp\n"
                + "OLp1mllQxsVTHBB8p1/7wkultjZxPc6m2zEv0DhKdNcypCoDAQKCAQEA/cmNFUhf\n"
                + "vG0bUmB7CA3dxRfWga9pT5bzkvaey0htRNhBQXqDjHIupa5LjL8Y1g9QgiTn149V\n"
                + "qjV0C7F4RyzKwGTiEkz4iBESHK+bg4tMBfyp7MNppVsu3645dFkI67nRjG5EJc/V\n"
                + "XxAzzFKmYf2eEKhpuw0HwBZfEqTb105LRNiIECU3b2XEF/xfRr7rHSmQdQDuTWjr\n"
                + "86YKzWCKBRss3QYnwUpeB0MbJ1H0Dyb9GJJsPIySY4ufBuG8ZcDbCcOrRFocIMMK\n"
                + "3KMotgbN2YM3bTpfSeK82QdfD/fLJQaKu/GLO0IuwLTCUdidTStpiAOyUAd8aTce\n"
                + "Jyip9hzJg54chwKCAQEAnwfdvBHpQYPvD6KEgT0zMgMaPHEHG5zt01NhekXiRmfs\n"
                + "WMYHsHiUtboDJner3+V43FQQz/GhWe8LZU0SwzbkeSCYzRMR3VKYSFOx7s6aRQ4D\n"
                + "IwBnK2777pM2B880iFodvQlTeACBUKV3mt3fxTNVbOs3rqs8wC0ODJZIZ+42fq9q\n"
                + "Oa1/YELYlnwbhSaZp+r2f0zEbNuLD2kzUz+8pbXJKbPkMtQqx6JwlPh01MY3zbqg\n"
                + "ReITL51VTU53EiOa0U+ADz6uL3B2nw8DTqg9nWw6LUmyNLleKoeaOV+95oekzzJ3\n"
                + "9AlYSyqac8MJkJOiiIiyeJg9vKZYmTeTcvtL/NtdgQKCAQEAoauEsZsiSbGjpw2J\n"
                + "Mq9KqGSwJHsu9iGuVt++drdTzHiK0YCPTqfqaWcn/6g41Rx6Z/3Ep4BKzRwyKcTL\n"
                + "X2P8YSWjEo9v/5YIWLfRtLHHI0U6pnYx1cHJkXq2ZRTW5vu/rtsLlJ7aSS3UIYRB\n"
                + "M8lRqUDv4dXCKy7VL9ZPqc/ZiSj7PHXI47ELg1AlDbdPpYs12CNYq318WgFbfkvS\n"
                + "gMA4CzEBoFOUpMGuCZVeiUyIDOAyDTxrgPiPvN2Om6+ImabJcsiIhKJbSAS0SYj6\n"
                + "F2dMpst5qmLDdOoKN+zdv190f5e233AgwmgkJel9A4z1NE1OiUbLjWcsUTvJUdwy\n"
                + "zyKo/wKCAQAQIcQkZ8y5kKCXfWzjj0m6MQZgSzblXi3h2ftxY9VoPvKCrtPo2tJ6\n"
                + "/LuFE26j76sq7nwmG+S6Mr19MSxOEStr/hqB8wVE5jP8YkEScHLFvn4i9s+AYGm9\n"
                + "8cDxWduCWWHa4y9MZQC5JY/Ubd1dK6/mtJWZalVnSSq7rCL8J/XvM+wanbbmFOHT\n"
                + "ohNIlnnPxs3qa+chA8Q/c/R45WZFiQM278CeR1dvmNLCydFQJCtU+zF25U/87IDS\n"
                + "rrr1ZBc4VFAxO7J/rXDbAbLcL8TQS0I7hdZF8ufSeJ70YvnogKn/OqdgYfJK7a9t\n"
                + "PsOhnthF8VfpU8gvctBZ+oFCkKtMoxQBAoIBAHELzCrBriRcnFjb1uUNcolNRi0Z\n"
                + "GucGpGJA7InjZhGi3v/Jkklh7VXA3EcHC8o7W+hvniY7QFVLsYn8svWvljY9+nNx\n"
                + "OeknmXHVUng74NRSM9SPTlnopaT/4C8+q/jzHiPdiDCJqBH64w70Np37OSMgwvpw\n"
                + "XAEBGy1YRST3UWGX6oZwmE5Pf9FurWk5Ws9TYiE5/rfhrAnIEFFYOO9OEo8PJ48s\n"
                + "75F3pJaYsKq+aGSham/310DpFoxss8yeWs/aqEN+ceIDccncdWXwOosBpk2GLhhQ\n"
                + "SdbDyf8QTSf+xN3ihfUIf5XbB3cna6rdLCPBT2i80kdTlqmihebxthBkgdQ=\n"
                + "-----END RSA PRIVATE KEY-----\n"
                + "";

        crt = "-----BEGIN CERTIFICATE-----\n"
                + "MIIGkTCCBHmgAwIBAgIGAVVWR2MaMA0GCSqGSIb3DQEBCwUAMHoxDDAKBgNVBAMT\n"
                + "A0lNRDEbMBkGA1UECxMSQ2xvdWQgTG9hZEJhbGFuY2VyMRowGAYDVQQKExFSYWNr\n"
                + "c3BhY2UgSG9zdGluZzEUMBIGA1UEBxMLU2FuIEFudG9uaW8xDjAMBgNVBAgTBVRl\n"
                + "eGFzMQswCQYDVQQGEwJVUzAeFw0xNjA2MTUyMjU2MDZaFw0yNzA4MzEyMjU2MDZa\n"
                + "MIGGMRgwFgYDVQQDEw93d3cucmFja2V4cC5vcmcxGzAZBgNVBAsTEkNsb3VkIExv\n"
                + "YWRCYWxhbmNlcjEaMBgGA1UEChMRUmFja3NwYWNlIEhvc3RpbmcxFDASBgNVBAcT\n"
                + "C1NhbiBBbnRvbmlvMQ4wDAYDVQQIEwVUZXhhczELMAkGA1UEBhMCVVMwggIiMA0G\n"
                + "CSqGSIb3DQEBAQUAA4ICDwAwggIKAoICAQCdp/r0hBkLhIEN+HBbPmy+HpUc6HBo\n"
                + "pchQIHKAp/ynwgjukiayWTAfYIbRP/d8e1P219Nv/t4A2UE5A/mkBh0HweC52MYI\n"
                + "udJcWN/iwUonMUeCo4wAHMx1ksPNJifWnBHTd5jHppFioEd0P8whFOyl1TsUsqRM\n"
                + "MKR4T+qVtq10FkNWbIgmOBzA9086cdsA3leYOIQspa2m+QqCHIU5owJqclIaWMcg\n"
                + "3yJs69hlSP9AoNoFDKfU8bFexwgFi+eflcIS3DmEmgfFMdfeNg1y0BsT/bnUnRU5\n"
                + "OErSipbjNcbWmaK+HDMSgEAh9D11z2TG2y/PdO8GL87zi66HgHaAugTUCoqUnYEj\n"
                + "3qGJSNedTWZei+P0FwLRwT0RlaoyzPbQyG68SYstIA3OYonTtY5y+f776bvTqG4q\n"
                + "d2LqBP/ajXSKkX21pjUtCfAAQ21OroTU1OK20R4B6ho2bgL2bwMwPw2lQRie9pnA\n"
                + "NmxaBxFSmaUyLBYTlG4ad8/O9A6MHFUAmeGQcTJLSFM66mLhzsRbqdzdfegvQCaY\n"
                + "PJm82bIEMOlAzIMga/aXb5b1qP0SzQl7KRNiTTXbPiFVpvj+xSYyO+KS1HNgGDNo\n"
                + "VzEGRbY1m1qvslRLZ2fV6OaWhoHEew0NEZagKIC2DMnumhSn4JSus6SgRO2bAwGU\n"
                + "ZP3Z1atADoZrBwIDAQABo4IBDjCCAQowDAYDVR0TAQH/BAIwADAOBgNVHQ8BAf8E\n"
                + "BAMCBLAwIAYDVR0lAQH/BBYwFAYIKwYBBQUHAwEGCCsGAQUFBwMCMIGoBgNVHSME\n"
                + "gaAwgZ2AFBmALcnULZGNnFRkqv22DqOWgoh9oX2kezB5MQswCQYDVQQDEwJDQTEb\n"
                + "MBkGA1UECxMSQ2xvdWQgTG9hZEJhbGFuY2VyMRowGAYDVQQKExFSYWNrc3BhY2Ug\n"
                + "SG9zdGluZzEUMBIGA1UEBxMLU2FuIEFudG9uaW8xDjAMBgNVBAgTBVRleGFzMQsw\n"
                + "CQYDVQQGEwJVU4IGAVVWRpO6MB0GA1UdDgQWBBQ2FvpmWgnWiP5TGldjYZ3gyPsE\n"
                + "ITANBgkqhkiG9w0BAQsFAAOCAgEAqcfuim4iiDSNIRseRurff0pjAm4kvvRHGjAU\n"
                + "5S5JXap4DM/nJn7rBE22NVXQbCr0PksmAmPY/bqZKptfQdhT6h8jAImY6zlL4Obc\n"
                + "vQkrnAZjaBDeefYfucgU0GwtwlkUXn5ERIa97Q+Ff/mckemQQJuLIPu5DgvDxE99\n"
                + "AX2fVhBU3YYkdE690TeB45aeEQJIvb8PAM46vTpRxFwLuq+8hQB1Ir0x+LY3IBSA\n"
                + "pL4NE0LkWAbyIwv5tkUFx1mFjjblP0YVaYEbGvQbatHAc7eCDFHxh2TggWer/x/Y\n"
                + "b16TbH1C8H0aEfYU4o/IiMpXFC5mMvLwGfOy/vG+stgxOy2FkEFIRm7yoiZasMrb\n"
                + "BfccM2zjXWfWfG4PwcQ8xqt9ISegfpDNe4k0z8sU22BcdGnwdjZEJ6zBweXnL4bm\n"
                + "vGFQjIxxRn1IqaZk74rVlTkI82IJyGg+iXPJ9qG1QjXLkD/JHtA/xO7aZ8Ij65VY\n"
                + "9WWhWpjbjxCvTLQIKGW58tu5N/qlDHNr5DcSsjq7Nf0OFgaxPe03p3B5x3V8VRyN\n"
                + "CzlgPauRTtm+mB8vjKnA0F4HFyVsGsdMMWAR4tvPUluXRNkh+V5gb8FbscL2sbu9\n"
                + "WAbSVtgKkUe7/DPPuF09L3Gubq0pwHW7SoS2edSepBbqFqT0eNXrlAGiWAwhDpq3\n"
                + "NbAQvJ4=\n"
                + "-----END CERTIFICATE-----\n"
                + "";

        imd = "-----BEGIN CERTIFICATE-----\n"
                + "MIIGgTCCBGmgAwIBAgIGAVVWRpO6MA0GCSqGSIb3DQEBCwUAMHkxCzAJBgNVBAMT\n"
                + "AkNBMRswGQYDVQQLExJDbG91ZCBMb2FkQmFsYW5jZXIxGjAYBgNVBAoTEVJhY2tz\n"
                + "cGFjZSBIb3N0aW5nMRQwEgYDVQQHEwtTYW4gQW50b25pbzEOMAwGA1UECBMFVGV4\n"
                + "YXMxCzAJBgNVBAYTAlVTMB4XDTE2MDYxNTIyNTUxM1oXDTI3MDkwMTIyNTUxM1ow\n"
                + "ejEMMAoGA1UEAxMDSU1EMRswGQYDVQQLExJDbG91ZCBMb2FkQmFsYW5jZXIxGjAY\n"
                + "BgNVBAoTEVJhY2tzcGFjZSBIb3N0aW5nMRQwEgYDVQQHEwtTYW4gQW50b25pbzEO\n"
                + "MAwGA1UECBMFVGV4YXMxCzAJBgNVBAYTAlVTMIICIjANBgkqhkiG9w0BAQEFAAOC\n"
                + "Ag8AMIICCgKCAgEAqrSzGbLwNx/KRj5f9EIprvohdrWV/HHF6gTM/Ph26GwtacAb\n"
                + "A7P6IpZMxRvRYYHLsaf+KLhMBx6g0mLoOwLAzsJN6eP0HKptZ7T5uR3XWv620FqP\n"
                + "jEwg+yuOB7wbQbQYYA53di9sbr6YQjAfutFWSuyebv7klYnDRp893VhqIGA5c8tD\n"
                + "o4Lpu2RGDs0oZoXOqSzZXxlAbUnufF2fkDUiIPiPlrK5QcquqW5ooxkRdIwGKvHl\n"
                + "+OlwyGdVmxUJ4N07/wz4ca1txkwx9PHPe7Qh9k9BAyytybh87SBg6KvFhrcHSXuv\n"
                + "MdWuTWiKtXpQs6qoZuoWPp5b4KkWxq9YP7njMoe8ONSQ+fiJw4GVUBD2gh0m3YOo\n"
                + "/liHZyoEH2aHX9NqscDamLti0/pKIHYFvTsbuEPMVMNVBRoIRKcwUZRuXoTruOSx\n"
                + "mbG+o4w/VHBTJGY6elvNRq36H3p3PiV0wxDdXYlTyO5Jsn+kDB5f5IHRTkTrx06u\n"
                + "uv65mq3Hco8jPUaU/mHa5CVsPMSjeW/aGxDPZ5VeumER+RsobRSZtTP5+SLQ0iIx\n"
                + "uuRuAsZ3FX7mN5m4X1kyuzgG7C2dD0MfPHPR2NWjRSNcQws1NBsbhE9crd1wm5Pc\n"
                + "fHYD3EL/7+bLZ9kPfP1iPTU6pV7ncWbQqa6BUTO1WxsGN0A6mIPVhm6TiJkCAwEA\n"
                + "AaOCAQwwggEIMA8GA1UdEwEB/wQFMAMBAf8wDgYDVR0PAQH/BAQDAgK0MCAGA1Ud\n"
                + "JQEB/wQWMBQGCCsGAQUFBwMBBggrBgEFBQcDAjCBowYDVR0jBIGbMIGYgBStGewq\n"
                + "ibdL3DvzARt5hrQwVP06O6F9pHsweTELMAkGA1UEAxMCQ0ExGzAZBgNVBAsTEkNs\n"
                + "b3VkIExvYWRCYWxhbmNlcjEaMBgGA1UEChMRUmFja3NwYWNlIEhvc3RpbmcxFDAS\n"
                + "BgNVBAcTC1NhbiBBbnRvbmlvMQ4wDAYDVQQIEwVUZXhhczELMAkGA1UEBhMCVVOC\n"
                + "AQEwHQYDVR0OBBYEFBmALcnULZGNnFRkqv22DqOWgoh9MA0GCSqGSIb3DQEBCwUA\n"
                + "A4ICAQAMIl3lwc6DjQ7V/WQDpPLyaKmkA7xUThx1HOPO/jOGbth0oHWgrGrjL+IX\n"
                + "SIead3+SElngibg69RLQHSIa+ESbuNn/5u+wa7cfrrXDmFmy+q6TSwZ9xUhdDg3n\n"
                + "VZxs4JgS+TWsRkto0GR5OoVB8OCUs/r2wYMHSrYaYQWjW9f9Cttiig+Adhz1YtrR\n"
                + "5yIyISxmukQ1fNHeKbGFEsuRKBdAPXAJxgjzlhZH268HfwHV4VLIzc6c6BaJQNah\n"
                + "1E+3c9AKL4gSaiToqbFp3CU5/zzeu2VgKjCkSlJLvF3L7dw3Rq2O7Fhep+3fTbCe\n"
                + "/WtPk2pdmbnJEn1df9FCqyqxQeslNnjY4MAcbKD+a/4oA8/c68Jw2pIaYxzPLBMJ\n"
                + "BALbLATZYocvJMZQaDM0n+9esTcFr4P/fy4Vz99h+Mj7XoBfsPyV5n/nFO19ZqiM\n"
                + "R7E3natI6sbP5Wlk77AjH/zm9ye/ZtUVxnRFBrhb/I5M+nkSoUFvJSUmAm+Ry0lc\n"
                + "4fDWcrgHVmZVA+y9n7CSOKcNRSCQIo8X9EQdPgYsmpMf0WUgYSbxgGLN5HwM3tCY\n"
                + "aHhZvyJXlEdW7siLZ/gmRruR0g4udh3Mmj7RjjE9zDQQNsbAGNT2gsyGxwRcr7c8\n"
                + "yxnoyJ1KUGhWzS0AyXkA2d/nctHrNGlx5mxFzDyCP/ZOvuSxeg==\n"
                + "-----END CERTIFICATE-----\n"
                + "";
        exampleKey = sniKeys[0];
    }

    @GET
    @Path("loadbalancers")
    public Response stubLoadBalancers() {
        LoadBalancers loadbalancers = new LoadBalancers();
        loadbalancers.getLoadBalancers().add(newLoadBalancer(1, "LB1"));
        loadbalancers.getLoadBalancers().add(newLoadBalancer(2, "LB2"));
        List<Link> links = loadbalancers.getLinks();
        Link link = new Link();
        link.setHref("someHref");
        link.setRel("someRel");
        links.add(link);
        link = new Link();
        link.setHref("anotherHref");
        link.setRel("someOtherRel");
        links.add(link);
        return Response.status(200).entity(loadbalancers).build();
    }

    @GET
    @Path("loadbalancer")
    public Response stubLoadBalancer() {
        LoadBalancer lb = newLoadBalancer(1, "LB1");
        return Response.status(200).entity(lb).build();
    }

    @GET
    @Path("virtualip")
    public Response stubVirtualIp() {
        VirtualIp virtualIp = newVip(1, "127.0.0.1");
        return Response.status(200).entity(virtualIp).build();
    }

    @GET
    @Path("virtualips")
    public Response stubVirtualIps() {
        VirtualIps vips = new VirtualIps();
        vips.getVirtualIps().add(newVip(1, "127.0.0.1"));
        vips.getVirtualIps().add(newVip(2, "127.0.0.2"));
        return Response.status(200).entity(vips).build();
    }

    @GET
    @Path("connectionthrottle")
    public Response stubConnectionThrottle() {
        ConnectionThrottle ct;
        ct = new org.openstack.atlas.docs.loadbalancers.api.v1.ConnectionThrottle();
        ct.setMaxConnectionRate(100);
        ct.setMaxConnections(200);
        ct.setMinConnections(300);
        ct.setRateInterval(60);
        return Response.status(200).entity(ct).build();
    }

    @GET
    @Path("node")
    public Response stubNode() {
        Node node;
        node = newNode(64, 80, "127.0.0.1");
        return Response.status(200).entity(node).build();
    }

    @GET
    @Path("uri")
    public Response uriInfo() {
        String uri = getRequestStateContainer().getUriInfo().getAbsolutePath().toString();
        SourceAddresses sa = new SourceAddresses();
        sa.setIpv4Public(uri);
        return Response.status(200).entity(sa).build();
    }

    @GET
    @Path("healthmonitor")
    public Response stubHealthMonitor() {
        HealthMonitor hm;
        hm = new HealthMonitor();
        hm.setAttemptsBeforeDeactivation(10);
        hm.setBodyRegex(".*");
        hm.setDelay(60);
        hm.setId(64);
        hm.setPath("/");
        hm.setStatusRegex(".*");
        hm.setTimeout(100);
        hm.setType(HealthMonitorType.HTTP);
        return Response.status(200).entity(hm).build();
    }

    @GET
    @Path("sessionpersistence")
    public Response stubSessionPersistence() {
        SessionPersistence sp;
        sp = new SessionPersistence();
        sp.setPersistenceType(PersistenceType.HTTP_COOKIE);
        return Response.status(200).entity(sp).build();
    }

    @GET
    @Path("connectionlogging")
    public Response stubConnectionLogging() {
        ConnectionLogging cl;
        cl = new ConnectionLogging();
        cl.setEnabled(Boolean.TRUE);
        return Response.status(200).entity(cl).build();
    }

    @GET
    @Path("nodes")
    public Response stubNodes() {
        List<Node> nodeList;
        Nodes nodes;
        Node node;
        nodes = new Nodes();
        nodeList = nodes.getNodes();
        nodeList.add(newNode(64, 80, "127.0.0.1"));
        nodeList.add(newNode(64, 443, "127.0.0.2"));
        return Response.status(200).entity(nodes).build();
    }

    @GET
    @Path("accesslist")
    public Response stubAccessList() {
        AccessList al = new AccessList();
        al.getNetworkItems().add(newNetworkItem(1, "10.0.0.0/8"));
        al.getNetworkItems().add(newNetworkItem(2, "192.168.0.0/24"));
        return Response.status(200).entity(al).build();
    }

    @GET
    @Path("updated")
    public Response stubUpdated() {
        Calendar now = Calendar.getInstance();
        Updated updated = new Updated();
        updated.setTime(now);
        return Response.status(200).entity(updated).build();
    }

    @GET
    @Path("errorpage")
    public Response stubErrorPage() {
        String format = "<html><big><big><big><big><big><big>%s</big></big></big></big></big></big></html>";
        String msg = String.format(format, "<b>Error or something happened</b>");
        Errorpage errorpage = new Errorpage();
        errorpage.setContent(msg);
        return Response.status(200).entity(errorpage).build();
    }

    @GET
    @Path("certificatemapping")
    public Response stubCertificateMapping() throws UnsupportedEncodingException, RsaException, InvalidKeySpecException {
        KeyPair kp = newKeyOrCache();
        long nowMillis = System.currentTimeMillis();
        Date notBefore = new Date(nowMillis);
        Date notAfter = new Date(nowMillis + 365L * millisPerDay);
        String hostName = String.format("www.host%d.org", incHostNameCount());
        String subj = String.format("C=US,ST=Texas,L=San Antonio,O=Rackspace Hostin,OU=CloudLoadbalancers,CN=%s", hostName);
        PKCS10CertificationRequest csr = CsrUtils.newCsr(subj, kp, true);
        BigInteger serial = BigInteger.valueOf(nowMillis);
        X509Certificate x509 = CertUtils.selfSignCsrCA(csr, kp, notBefore, notAfter);
        byte[] crtBytes = PemUtils.toPem(x509);
        CertificateMapping cm = new CertificateMapping();
        cm.setHostName(hostName);
        cm.setCertificate(new String(crtBytes));
        cm.setPrivateKey(RSAKeyUtils.KeyPairToString(kp));
        cm.setId(getKeyGenCount());
        return Response.status(200).entity(cm).build();
    }

    @GET
    @Path("certificatemappings")
    public Response stubCertificateMappings() {
        CertificateMappings cms = new CertificateMappings();
        for (int i = 0; i < 3; i++) {
            CertificateMapping cm = new CertificateMapping();
            cm.setId(i + 1); // Just cause ID 0 looks funny
            cm.setCertificate(sniCrts[i]);
            cm.setPrivateKey(sniKeys[i]);
            cm.setHostName(sniHosts[i]);
            cms.getCertificateMappings().add(cm);
        }
        return Response.status(200).entity(cms).build();
    }

    @GET
    @Path("ssltermination")
    public Response stubSslTerm() {
        if (!ConfigurationHelper.isAllowed(restApiConfiguration, PublicApiServiceConfigurationKeys.ssl_termination)) {
            return Response.status(Response.Status.BAD_REQUEST).build();
        }



        SslTermination sslTermination = new SslTermination();
        sslTermination.setCertificate(crt);
        sslTermination.setPrivatekey(key);
        sslTermination.setIntermediateCertificate(imd);
        sslTermination.setEnabled(true);
        sslTermination.setSecurePort(443);
        sslTermination.setSecureTrafficOnly(false);
        SecurityProtocol protocol = new SecurityProtocol();
        protocol.setSecurityProtocolName(SecurityProtocolName.TLS_10);
        protocol.setSecurityProtocolStatus(SecurityProtocolStatus.DISABLED);
        sslTermination.getSecurityProtocols().add(protocol);
        return Response.status(Response.Status.OK).entity(sslTermination).build();
    }

    private Node newNode(Integer id, Integer port, String address) {
        Node node;
        node = new Node();
        node.setAddress(address);
        node.setCondition(NodeCondition.ENABLED);
        node.setId(id);
        node.setPort(port);
        node.setStatus(NodeStatus.ONLINE);
        node.setWeight(1);
        return node;
    }

    private VirtualIp newVip(Integer id, String address) {
        VirtualIp vip;
        vip = new VirtualIp();
        vip.setId(id);
        vip.setAddress(address);
        vip.setIpVersion(IpVersion.IPV4);
        vip.setType(VipType.PUBLIC);
        return vip;
    }

    private NetworkItem newNetworkItem(Integer id, String address) {
        NetworkItem n = new NetworkItem();
        n.setId(id);
        n.setAddress(address);
        n.setIpVersion(IpVersion.IPV4);
        n.setType(NetworkItemType.DENY);
        return n;
    }

    private LoadBalancer newLoadBalancer(Integer id, String name) {
        List<Node> nodes = new ArrayList<Node>();
        List<VirtualIp> vips = new ArrayList<VirtualIp>();
        List<NetworkItem> accessList = new ArrayList<NetworkItem>();
        LoadBalancer lb = new LoadBalancer();
        Created created = new Created();
        Updated updated = new Updated();
        created.setTime(Calendar.getInstance());
        updated.setTime(Calendar.getInstance());
        ConnectionThrottle ct = new ConnectionThrottle();
        Cluster cl = new Cluster();
        ConnectionLogging cnl = new ConnectionLogging();
        cnl.setEnabled(Boolean.TRUE);
        ct.setMaxConnectionRate(100);
        ct.setMaxConnections(200);
        ct.setMinConnections(300);
        ct.setRateInterval(60);
        cl.setName("TestCluster");
        lb.setName(name);
        lb.setAlgorithm("RANDOM");
        lb.setCluster(cl);
        lb.setConnectionLogging(cnl);
        lb.setConnectionThrottle(ct);
        lb.setPort(80);
        lb.setProtocol("HTTP");
        lb.setStatus("BUILD");
        lb.setCreated(created);
        lb.setUpdated(updated);
        nodes.add(newNode(1, 80, "127.0.0.10"));
        nodes.add(newNode(1, 443, "127.0.0.20"));
        vips.add(newVip(1, "127.0.0.1"));
        vips.add(newVip(2, "127.0.0.2"));
        lb.setVirtualIps(vips);
        lb.setNodes(nodes);
        SessionPersistence sp = new SessionPersistence();
        sp.setPersistenceType(PersistenceType.HTTP_COOKIE);
        lb.setSessionPersistence(sp);
        accessList.add(newNetworkItem(1, "10.0.0.0/8"));
        accessList.add(newNetworkItem(2, "192.168.0.0/24"));
        lb.setAccessList(accessList);
        LoadBalancerUsage lu = new LoadBalancerUsage();
        lu.setLoadBalancerId(id);
        lu.setLoadBalancerName(name);
        lu.getLoadBalancerUsageRecords().add(newLoadBalancerUsageRecord(1));
        lu.getLoadBalancerUsageRecords().add(newLoadBalancerUsageRecord(2));
        lb.setLoadBalancerUsage(lu);
        return lb;
    }

    private LoadBalancerUsageRecord newLoadBalancerUsageRecord(Integer id) {
        LoadBalancerUsageRecord ur = new LoadBalancerUsageRecord();
        ur.setAverageNumConnections(3.0);
        ur.setId(id);
        ur.setEventType("EmptyEvent");
        ur.setIncomingTransfer(new Long(20));
        ur.setNumPolls(50);
        ur.setNumVips(30);
        ur.setOutgoingTransfer(new Long(30));
        ur.setEndTime(Calendar.getInstance());
        return ur;
    }

    public KeyPair newKeyOrCache() throws RsaException, InvalidKeySpecException, UnsupportedEncodingException {
        long nowMillis;
        long delta;
        boolean isThisThreadBuildingKey = false;
        KeyPair oldCachedKey;
        String tmpKey;
        synchronized (exampleKeyLock) {
            tmpKey = exampleKey;
        }
        oldCachedKey = RSAKeyUtils.getKeyPairFromString(tmpKey);

        synchronized (lastKeyReadLock) {
            synchronized (isKeyBuildingLock) {
                if (isKeyBuilding) {
                    return oldCachedKey;
                }
                nowMillis = System.currentTimeMillis();
                delta = nowMillis - lastKeyRead;
                if (nowMillis - lastKeyRead > cacheTimeMillis) {
                    isKeyBuilding = true;
                    isThisThreadBuildingKey = true;
                    lastKeyRead = nowMillis; // To make sure no one else tries to build the key
                } else {
                    return oldCachedKey;
                }
            }
        }

        if (isThisThreadBuildingKey ) {
            KeyPair kp = RSAKeyUtils.genKeyPair(4096);
            String newKey = RSAKeyUtils.KeyPairToString(kp);
            incKeyGenCount();
            synchronized (exampleKeyLock) {
                exampleKey = newKey;
                oldCachedKey = kp;
            }
            synchronized (lastKeyReadLock) {
                synchronized (isKeyBuildingLock) {
                    lastKeyRead = System.currentTimeMillis();
                    isKeyBuilding = false;
                }
            }
            return oldCachedKey;
        }
        return oldCachedKey;
    }

    public static synchronized int incKeyGenCount() {
        keyGenCount++;
        return keyGenCount;
    }

    public static synchronized int getKeyGenCount() {
        return keyGenCount;
    }

    public static synchronized int incHostNameCount() {
        certMapHostNameCount++;
        return certMapHostNameCount;
    }
}
