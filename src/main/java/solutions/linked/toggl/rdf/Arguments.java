/*
 * The MIT License
 *
 * Copyright 2017 user.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package solutions.linked.toggl.rdf;

import java.util.Date;
import org.wymiwyg.commons.util.arguments.CommandLine;

/**
 *
 * @author user
 */
public interface Arguments {
    
    @CommandLine (
        longName ="api-key",
        shortName = "K", 
        required = true,
        description = "The API-key you see on your Toggl profile page"
    )
    public String apiKeys();
    
    @CommandLine (
        longName ="since",
        shortName = "S", 
        required = false,
        description = "The date and optionally time since which to dump entries, defaults to one day before the until-date"
    )
    public String since();
    
    @CommandLine (
        longName ="until",
        shortName = "U", 
        required = false,
        description = "The date and optionally time until which to dump entries, defaults to the current time"
    )
    public String until();
    
    @CommandLine (
        longName ="requestInterval",
        shortName = "I",
        required = false,
        defaultValue = "24",
        description = "The duration of the interval of a single API call in hours, the period to dump is split into period of at most this length"
    )
    public int requestInterval();
    
    @CommandLine (
        longName ="format",
        shortName = "F", 
        required = false,
        defaultValue = "text/turtle",
        description = "The desired output format (e.g. text/turtle, application/rdf+xml)"
    )
    public String format();

    
}
