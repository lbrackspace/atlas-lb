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
# ruby global_error_file.rb --hostname=lb-n01.example.com --username=admin --password=pass  > tmp.txt && cat tmp.txt

@options = {}
opts = OptionParser.new
opts.on("-h", "--hostname=hostname")  { |hostname|  @options[:hostname] = hostname }
opts.on("-u", "--username=username")  { |username|  @options[:username] = username }
opts.on("-p", "--password=password")  { |password|  @options[:password] = password }
opts.parse!

@options[:rule_name] = "global_error.html"

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
  
  def get_virtual_server_protocol virtual_server
    
    p = proxy "http://soap.zeus.com/zxtm/1.0/VirtualServer/"
    p.add_method "getProtocol", "names"
    val = p.getProtocol [virtual_server]
    val[0]
    
  end
  
  def get_error_file_by_virtual_server virtual_server
    
    p = proxy "http://soap.zeus.com/zxtm/1.0/VirtualServer/"
    p.add_method "getErrorFile", "names"
    val = p.getErrorFile [virtual_server]
    val[0]
    
  end
  
  def set_error_file virtual_server
    
    default_file = "global_error.html"
    
    p = proxy "http://soap.zeus.com/zxtm/1.0/VirtualServer/"
    p.add_method "setErrorFile", "names", "values"
    val = p.setErrorFile [virtual_server], [default_file]
    val
    
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

virtual_servers.each do |virtual_server|
  
  protocol = zeus.get_virtual_server_protocol virtual_server
  puts "*** VS #{virtual_server} is of type #{protocol} ***"
  
  if protocol == "http"
    
    error_file = zeus.get_error_file_by_virtual_server virtual_server

    puts "*** VS #{virtual_server} has error file: #{error_file.inspect}"
    unless error_file.include? @options[:rule_name]
      
      begin
        
        zeus.set_error_file virtual_server
        puts "*** Adding #{@options[:rule_name]} to #{virtual_server} ***"
        
      rescue Exception => e
        
        puts "*** FAILED adding #{@options[:rule_name]} to #{virtual_server}: (#{e.class}) #{e.message} ***"
        
      end
      
    else
      
      puts "*** Skipping #{virtual_server} it already has error file #{@options[:rule_name]} ***"
      
    end
  
  else
    
    puts "*** Skipping #{virtual_server} because it is of type #{protocol} ***"
    
  end
  
end
