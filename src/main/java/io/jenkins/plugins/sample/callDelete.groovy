package io.jenkins.plugins.sample;
import groovy.json.*
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

/**
 *
 * @author tigerbaylimited
 */

class GetNodes {
    def exclude
    def cloudType 
    def deleteType 
    def deleteLabel
    def vmCount
    def fetchAPI
    def apiUsername
    def apiPassword
    def jsonName
    def jsonDeleteParam
    def awsID
    def awsKey
    def awsRegion

def loopnodes(exclude,cloudType,deleteType,deleteLabel,vmCount,fetchAPI,apiUsername,apiPassword,jsonName,jsonDeleteParam,awsID,awsKey,awsRegion){
    
  println('==================== Printing ALL SYSTEM SLAVES ====================');

        for (aSlave in hudson.model.Hudson.instance.slaves) { //First instance of hudson to loop through nodes. aSlave is the each slave in Jenkins.

  println('Name: ' + aSlave.name);
  println('getLabelString: ' + aSlave.getLabelString());
 println('getNumExectutors: ' + aSlave.getNumExecutors());
  println('getRemoteFS: ' + aSlave.getRemoteFS());
   println('getMode: ' + aSlave.getMode());
  println('getRootPath: ' + aSlave.getRootPath());
   println('getDescriptor: ' + aSlave.getDescriptor());
   println('getComputer: ' + aSlave.getComputer());
  println('\tcomputer.isAcceptingTasks: ' + aSlave.getComputer().isAcceptingTasks());
  println('\tcomputer.getConnectTime: ' + aSlave.getComputer().getConnectTime());
   println('\tcomputer.isOffline: ' + aSlave.getComputer().isOffline());
     println('================================================================== ');
     

     }
     
   if (cloudType == "ec2"){
       def String[] AWSparamArguments = [exclude,cloudType,deleteType,deleteLabel,vmCount,awsID,awsKey,awsRegion];
       println('==================== Cloud Option has been set to Amazon EC2 ====================');
                println("Now calling the deleteAWS.groovy Script");
   
    
    File sourceFile = new File("src/main/java/io/jenkins/plugins/sample/deleteAWS.groovy");
    Class groovyClass = new GroovyClassLoader(getClass().getClassLoader()).parseClass(sourceFile);
    GroovyObject groovyObj = (GroovyObject) groovyClass.newInstance();
 
    groovyObj.invokeMethod("selection", AWSparamArguments);
            
     
   }
  else  if (cloudType == "google"){
       println('==================== Cloud Option has been set to Google Cloud ====================');
                 println("Now calling the deleteGoogle.groovy Script");
   }
   else  if (cloudType == "azure"){
       println('==================== Cloud Option has been set to AZURE ====================');
               println("Now calling the deleteAzure.groovy Script");
   }
    else  if (cloudType == "ibm"){
       println('==================== Cloud Option has been set to IBM Cloud ====================');
               println("Now calling the deleteIBM.groovy Script");
   }
   else  if (cloudType == "custom"){
       println('==================== Cloud Option has been set to Custom REST API ====================');
               println("Now calling the deleteCustomAPI.groovy Script");
           
             
    def String[] CustomparamArguments = [exclude,cloudType,deleteType,deleteLabel,vmCount,fetchAPI,apiUsername,apiPassword,jsonName,jsonDeleteParam];
    File sourceFile = new File("src/main/java/io/jenkins/plugins/sample/deletePrivate.groovy");
    Class groovyClass = new GroovyClassLoader(getClass().getClassLoader()).parseClass(sourceFile);
    GroovyObject groovyObj = (GroovyObject) groovyClass.newInstance();
 
    groovyObj.invokeMethod("selection", CustomparamArguments);
  
   }
 }
}

