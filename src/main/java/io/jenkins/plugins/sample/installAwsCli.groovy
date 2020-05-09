/*
 * The MIT License
 *
 * Copyright 2020 tigerbaylimited.
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

package io.jenkins.plugins.sample
import groovy.json.*
/**
 *
 * @author tigerbaylimited
 */
class installAWS {
    
 def installCli(def awsID, def awsKey,def awsRegion){
  def proc = "sudo curl -o awscliv2.zip https://awscli.amazonaws.com/awscli-exe-linux-x86_64.zip".execute()
  println("started waiting")
  proc.consumeProcessOutput(System.out, System.err)
  proc.waitFor()
  println("stopped waiting")
  // cURL uses error output stream for progress output.
// Wait until cURL process finished and continue with the loop.

def proc2 = "sudo unzip -o awscliv2.zip".execute()
proc2.consumeProcessOutput(System.out, System.err)
   proc2.waitFor()

def proc3="sudo ./aws/install".execute()
proc3.consumeProcessOutput(System.out, System.err)
 proc3.waitFor()

def proc4 = "aws configure set ${awsRegion}".execute()
proc4.consumeProcessOutput(System.out, System.err)
proc4.waitFor()

            
def proc5 = "aws configure set aws_access_key_id '${awsID}'".execute()
proc5.consumeProcessOutput(System.out, System.err)
proc5.waitFor()

            
def proc6 = "aws configure set aws_secret_access_key '${awsKey}'".execute()
proc6.consumeProcessOutput(System.out, System.err)
proc6.waitFor()

        }
}

