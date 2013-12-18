package org.openstack.atlas.logs.hadoop.sequencefiles;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.SequenceFile;
import org.apache.hadoop.util.ReflectionUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.openstack.atlas.util.debug.Debug;
import org.openstack.atlas.util.staticutils.StaticStringUtils;

// Simulare to an Enumerator but allows Exceptions to be thrown
public class SequenceFileIterator<K extends Writable, V extends Writable> {

    private static final Log LOG = LogFactory.getLog(SequenceFileIterator.class);
    protected SequenceFile.Reader reader;
    protected FileSystem fs;
    protected Configuration conf;
    protected Path path;
    protected Class keyClass;
    protected Class valueClass;
    protected int entryNumber = 0;

    public SequenceFileIterator(Path path, FileSystem fileSystem) throws SequenceFileReaderException {
        fs = fileSystem;
        conf = fs.getConf();
        this.path = path;
        try {
            reader = new SequenceFile.Reader(fs, new Path(path.toUri().getPath()), conf);
        } catch (IOException ex) {
            String excMsg = Debug.getExtendedStackTrace(ex);
            String msg = String.format("IOException opening Sequence file %s for reading", path.toString());
            LOG.error(String.format("%s: %s", msg, excMsg), ex);
            throw new SequenceFileReaderException(msg, ex);
        }
        keyClass = reader.getKeyClass();
        valueClass = reader.getValueClass();
    }

    public SequenceFileEntry<K, V> getNextEntry() throws SequenceFileReaderException, EndOfIteratorException {
        K key = (K) ReflectionUtils.newInstance(keyClass, conf);
        V value = (V) ReflectionUtils.newInstance(valueClass, conf);
        boolean hasNext;
        try {
            hasNext = reader.next(key, value);
        } catch (IOException ex) {
            String excMsg = Debug.getExtendedStackTrace(ex);
            String msg = String.format("IOException while attempting to read sequence file %s", path.toString());
            LOG.error(String.format("%s: %s", msg, excMsg), ex);
            throw new SequenceFileReaderException(msg, ex);
        }
        if (!hasNext) {
            throw new EndOfIteratorException();
        }
        SequenceFileEntry<K, V> out = new SequenceFileEntry(this.path.toUri().toString(), entryNumber, key, value);
        entryNumber++;
        return out;
    }

    public void close() {
        try {
            reader.close();
        } catch (IOException ex) {
            LOG.warn(String.format("Coulden't close SequenceFileReader for %s assuming its already closed", path.toString()));
        }
    }
}
