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
# ruby x_forwarded_for.rb --hostname=lb-n01.example.com --username=admin --password=pass  > tmp.txt && cat tmp.txt

@options = {}
opts = OptionParser.new
opts.on("-h", "--hostname=hostname")  { |hostname|  @options[:hostname] = hostname }
opts.on("-u", "--username=username")  { |username|  @options[:username] = username }
opts.on("-p", "--password=password")  { |password|  @options[:password] = password }
opts.parse!

@options[:rule_name] = "add_x_forwarded_for_header"

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
  
  def get_rules_by_virtual_server virtual_server
    
    p = proxy "http://soap.zeus.com/zxtm/1.0/VirtualServer/"
    p.add_method "getRules", "names"
    val = p.getRules [virtual_server]
    val[0]
    
  end
  
  def add_rule virtual_server, rule
    
    rule_hash = {"enabled" => "true", "name" => "#{rule}", "run_frequency" => "run_every"}
    
    p = proxy "http://soap.zeus.com/zxtm/1.0/VirtualServer/"
    p.add_method "addRules", "names", "rules"
    val = p.addRules [virtual_server], [[rule_hash]]
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
    
    rules_struct = zeus.get_rules_by_virtual_server virtual_server
    rules = []
    
    if rules_struct.length > 0
      
      rules_struct.each do |rule|
        
        rules << rules_struct[0].name

      end
      
    end
    
    puts "*** VS #{virtual_server} has rules: #{rules.inspect}"
    unless rules.include? @options[:rule_name]
      
      begin
        
        zeus.add_rule virtual_server, @options[:rule_name]
        puts "*** Adding #{@options[:rule_name]} to #{virtual_server} ***"
        
      rescue Exception => e
        
        puts "*** FAILED adding #{@options[:rule_name]} to #{virtual_server}: (#{e.class}) #{e.message} ***"
        
      end
      
    else
      
      puts "*** Skipping #{virtual_server} it already has rule #{@options[:rule_name]} ***"
      
    end
  
  else
    
    puts "*** Skipping #{virtual_server} because it is of type #{protocol} ***"
    
  end
  
end