#!/usr/bin/env ruby
require "rubygems"
require "httpclient"
require "soap/rpc/driver"
require "optparse"

# INSTALL GEMS:
# sudo gem install soap4r --no-ri --no-rdoc
# sudo gem install httpclient --no-ri --no-rdoc
#
# RUN LIKE THIS:
# ruby node_state.rb --hostname=lb-n01.example.com --username=admin --password=pass  > tmp.txt

@options = {}
opts = OptionParser.new
opts.on("-h", "--hostname=hostname")  { |hostname|  @options[:hostname] = hostname }
opts.on("-u", "--username=username")  { |username|  @options[:username] = username }
opts.on("-p", "--password=password")  { |password|  @options[:password] = password }
opts.parse!

class Zeus
  
  attr_accessor :host, :username, :password
  
  def initialize host = nil, username = nil, password = nil
    
    @host = host
    @username = username
    @password = password
    
  end
  
  def get_virtual_servers
    
    p = proxy "http://soap.zeus.com/zxtm/1.0/VirtualServer/"
    p.add_method "getVirtualServerNames"
    val = p.getVirtualServerNames
    val
    
  end
  
  def get_pool_by_virtual_server virtual_server
    
    p = proxy "http://soap.zeus.com/zxtm/1.0/VirtualServer/"
    p.add_method "getDefaultPool", "names"
    val = p.getDefaultPool [virtual_server]
    val[0]
    
  end
  
  def get_nodes_by_pool pool
    p = proxy "http://soap.zeus.com/zxtm/1.0/Pool/"
    p.add_method "getNodes", "names"
    val = p.getNodes [pool]
    val[0]
    
  end
  
  def get_node_status node
    
    p = proxy "http://soap.zeus.com/zxtm/1.0/System/Stats/"
    p.add_method "getNodeState", "names"
    val = p.getNodeState [node]
    val[0]
    
  end
  
  private
  def proxy service = nil
    
    soap_endpoint = "https://#{@host}:9090/soap"
    p = SOAP::RPC::Driver.new soap_endpoint, service
    p.options["protocol.http.ssl_config.verify_mode"] = OpenSSL::SSL::VERIFY_NONE
    p.options["protocol.http.basic_auth"] << [soap_endpoint, @username, @password]
    p
    
  end
  
end

zeus = Zeus.new @options[:hostname], @options[:username], @options[:password]
virtual_servers = zeus.get_virtual_servers

pools = []
virtual_servers.each do |virtual_server|
  
  pool = zeus.get_pool_by_virtual_server virtual_server
  pools << pool
  
end

pools.each do |pool|
  
  begin
    
    nodes_output = zeus.get_nodes_by_pool pool
    nodes = []
    nodes_output.each do |node|

      node_hash = {}
      node_array = node.split ":"
      node_hash["Address"] = node_array[0]
      node_hash["Port"] = node_array[1]
      nodes << node_hash

    end

    nodes.each do |node|

      begin

        node_status = zeus.get_node_status node

        if node_status == "alive"

          sql = "update node set status='ONLINE' where ip_address='#{node['Address']}' and port='#{node['Port']}';"

        else

          sql = "update node set status='OFFLINE' where ip_address='#{node['Address']}' and port='#{node['Port']}';"

        end

        puts "#{sql}"

      rescue Exception => e

        puts "/* FAILED to get status for NODE #{node.inspect} --- #{e.class}: #{e.message} */"

      end

    end
    
  rescue Exception => e
    
    puts "/* FAILED to get info for POOL #{pool.inspect} --- #{e.class}: #{e.message} */"
    
  end 
  
end