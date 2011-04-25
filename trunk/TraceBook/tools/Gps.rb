#!/usr/bin/ruby

########################################################################
 #
 # This file is part of TraceBook.
 #
 # TraceBook is free software: you can redistribute it and/or modify it
 # under the terms of the GNU General Public License as published by the
 # Free Software Foundation, either version 3 of the License, or (at
 # your option) any later version.
 #
 # TraceBook is distributed in the hope that it will be useful, but
 # WITHOUT ANY WARRANTY; without even the implied warranty of
 # MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 # General Public License for more details.
 #
 # You should have received a copy of the GNU General Public License
 # along with TraceBook. If not, see <http://www.gnu.org/licenses/>.
 #
########################################################################

require 'rubygems'
require 'geokit'
require 'net/telnet'

class GpsFak0r
    def initialize(opts = {})
        opts[:hostname] ||= 'localhost'
        opts[:port] ||= 5554

        open(opts[:hostname], opts[:port])
    end

    def open(hostname, port)
        @host = Net::Telnet::new('Host' => hostname,
                                 'Port' => port,
                                 'Prompt' => /OK/,
                                 'Timeout' => 10)
        reset
    end

    def reset
        @current_position = Geokit::LatLng.new(52.4559497304728, 13.2975200387581)
        send_position(@current_position)
    end

    def send_position(position, opts = {})
        opts[:direction] ||= 'Initial fix'
        opts[:distance] ||= 0

        cmd = "geo fix #{position.lng} #{position.lat} 65 7"
        STDERR.puts "Sending: #{cmd} (direction: #{opts[:direction]} // distance: #{(opts[:distance] * 10000).round / 10000.0}m)"

        @host.cmd(cmd)
    end

    def go(heading, n, opts = {})
        opts[:delay] ||= 5
        opts[:heading_var] ||= 30
        opts[:step] ||= 5
        opts[:step_var] ||= 5


        1.upto(n) do
            heading_var = (opts[:heading_var] > 0 ? (-1) ** rand(2) * rand(opts[:heading_var]) : 0)
            step_var = (opts[:step_var] > 0 ? rand(opts[:step_var]) : 0)
            old_position = @current_position
            @current_position = old_position.endpoint(heading + heading_var,
                                                      (opts[:step] + step_var) / 1000.0, :units => :kms)
            distance = old_position.distance_to(@current_position, :units => :kms) * 1000
            direction = old_position.heading_to(@current_position).round

            send_position(@current_position, :distance => distance, :direction => direction)
            sleep opts[:delay]
        end
    end

    def xml(path)
        s = File.open(path) { |fp| fp.readlines.join("\n") }
        waypoints = s.scan(/lat="(.*?)" lon="(.*?)"/)
        waypoints.each do |waypoint|
            send_position(Geokit::LatLng.new(waypoint[0], waypoint[1]))
            sleep 1
        end
    end

    def position
        @current_position.ll
    end

    def close
        @host.close
    end
end

@gps = GpsFak0r.new

while (true) do
    @gps.go(90, 50, :step => rand(5) + 5, :delay => 1)
    @gps.go(180, 50, :step => rand(5) + 5, :delay => 1)
end
