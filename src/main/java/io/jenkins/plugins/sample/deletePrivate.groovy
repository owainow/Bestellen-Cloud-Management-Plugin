package io.jenkins.plugins.sample;
import jenkins.model.Jenkins
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

class deletePrivate {
    
def id; 
int i;
def kush;
def delSuccess;
def exclude;
def excludeArray;
def apiUsername;
def apiPassword;
def status;
def jsonName;
def jsonDeleteParam;
def aSlave;
def json;
def fetchAPI;
def parsedJson;
def slaveList = [];
def slave;
int machinesDeleted;
def deletionMap;
def safeType;
def procDelete;
def fetchNodes;

 
def deleteNode(excludeArray,jsonName,jsonDeleteParam,json,fetchAPI,deleteType,originalvmCount,apiUsername,apiPassword,safeType){

 jsonName = "ROOT."+jsonName
 jsonDeleteParam = "ROOT."+jsonDeleteParam


//This is the line to support multi-level JSON. All machines are matched according to the user input in this one succicnt line. 
//Without this multi-level json such as machine.data.name would not be supported as groovy passes it as one.
deletionMap= json.findAll{ Eval.me('ROOT', it, jsonName) in slaveList }.collectEntries{ [Eval.me('ROOT', it, jsonName),Eval.me('ROOT',it, jsonDeleteParam)]  }

  for(entry in slaveList){


    if (excludeArray.contains(entry)&& machinesDeleted < originalvmCount){
       println("The machine " +entry+ " is featured in the exclude list")
       delSuccess = false;
   }

   else if (deletionMap.find{it.key==entry} && machinesDeleted < originalvmCount){
      println ("=====================================================")
     
                        id = deletionMap.find{it.key==entry}.value
                      
                        println ("Instance Name is: " + entry + " Instance ID is: " + id)
                         println('Removing node from Jenkins...');
                         
                
                
            
               if (apiUsername && apiPassword){ //IF Credentials are passed through use them
                   
                      println ("Now running curl -u ${apiUsername}:${apiPassword} -x DELETE ${fetchAPI}/${id}");
                      
                          if (safeType == "true"){
                               for (aSlave in hudson.model.Hudson.instance.slaves) {
                                   
                                if(aSlave.name.equals(slave)){
                         aSlave.getComputer().setTemporarilyOffline(true,null); //Set node as offline for saftey 
                         println("Setting {$slave} to offline for saftey.")
                         aSlave.getComputer().doDoDelete(); // Delete the node from Jenkins
                             println("Jenkins node {$slave} deleted.")
                             }
                                }
                     procDelete = "curl -u ${apiUsername}:${apiPassword} -x DELETE  ${fetchAPI}/${id}".execute()
                     returnJson = new JsonSlurper().parseText(procDelete.text)
                     println(returnJson)
                        }
                      }
                      
                
                 else{ //else do call without credentials
                  println ("Now running curl -x DELETE " +fetchAPI+"/"+id);
     
                        if (safeType == "true"){
                     for (aSlave in hudson.model.Hudson.instance.slaves) {
                                   
                                if(aSlave.name.equals(slave)){
                         aSlave.getComputer().setTemporarilyOffline(true,null); //Set node as offline for saftey 
                         println("Setting {$slave} to offline for saftey.")
                         aSlave.getComputer().doDoDelete(); // Delete the node from Jenkins
                             println("Jenkins node {$slave} deleted.")
                             }
                                }
                        procDelete = "curl -x DELETE ${fetchAPI}/${id}".execute().text
                        returnJson = new JsonSlurper().parseText(procDelete)
                        println(returnJson)
                        }
                        
                 }
    
                     
                       delSuccess = true;
                       machinesDeleted ++;
                       println("Machines delted so far: "+machinesDeleted)
                      }
      
                       
    else if (machinesDeleted >= originalvmCount){
        println ("=====================================================")
       println("Slave: " +entry +" will not be deleted as "+machinesDeleted+" machines have already been deleted." )
        deletionMap.remove(entry) // This removes the matched VM show it does not show in the report as being deleted as it has not been.
    }
    
    else {
                               println ("=====================================================")
                           println("Slave: " +entry +" could not be matched in the cloud")
                            println("It does meet the deletion requirements however is unable to be deleted")
                            println("It is possible that this is an Orpaned VM and requires manual investigation.")
                           delSuccess = false;
                       }

   }
            return machinesDeleted;
  }

     




def selection(exclude,cloudType,deleteType,deleteLabel,vmCount,fetchAPI,apiUsername,apiPassword,jsonName,jsonDeleteParam,safeType){
  if (vmCount == null){
      vmCount = Integer.MAX_Value
  }
   excludeArray= exclude.split(',')
    fetchNodes = "curl ${fetchAPI}".execute().text
   json = new groovy.json.JsonSlurper().parseText(fetchNodes)

          
    
   
   if (deleteType == "offline" ) {
       println('==================== Deletion Option of Offline VMs Commencing ====================');
       
            for (aSlave in hudson.model.Hudson.instance.slaves) {
                if (aSlave.getComputer().isOffline() == true){
                slaveList.add(aSlave.name)
                 }
            }
              deleteNode(excludeArray,jsonName,jsonDeleteParam,json,fetchAPI,deleteType,Integer.MAX_VALUE,apiUsername,apiPassword,safeType);      
                 
     }
     
        
  else if (deleteType == "busy") {
      println('==================== Deletion Option of VMs Not Accepting Tasks Commencing ====================');
        for (aSlave in hudson.model.Hudson.instance.slaves) {
                if (aSlave.getComputer().isAcceptingTasks() == false){
                  slaveList.add(aSlave.name)
                   
                 }   
                 deleteNode(excludeArray,json,jsonName,jsonDeleteParam,fetchAPI,deleteType,Integer.MAX_VALUE,apiUsername,apiPassword,safeType);
         }
     }
     
        
        
     else if (deleteType == "efficient") {
         println('==================== Deletion Option for Machines set to Efficent ====================');
          println('==================== Warning this is only a BETA based on data available to Jenkins ====================');
              
                int newvmCount = vmCount as Integer
                int originalvmCount = vmCount as Integer
              
              
              for (aSlave in hudson.model.Hudson.instance.slaves) {
                if (aSlave.getComputer().isOffline() == true ){
                     slaveList.add(aSlave.name)
                }
              }
                             println('================================================================= ')
                             println("Starting to delete the slaves that are currently offline.")
                             println('================================================================= ')
                 newvmCount = deleteNode(excludeArray,jsonName,jsonDeleteParam,json,fetchAPI,deleteType,originalvmCount,apiUsername,apiPassword,safeType)
                  
                          println("Machines deleted so far: " + newvmCount)
                          if (newvmCount >= originalvmCount){
                               println("Machines have now been deleted")
                          }
                          
               else{
                             println('================================================================= ')
                             println("Continuing to now delete the slaves that have been idle for over 86400ms (24 hours).")
                             println('================================================================= ')
                                  slaveList=[] 
        
                for (aSlave in hudson.model.Hudson.instance.slaves) {
                    if (aSlave.getComputer().getIdleStartMilliseconds() > 86400) {
                       
                     slaveList.add(aSlave.name)
                }
                }
                 newvmCount = deleteNode(excludeArray,jsonName,jsonDeleteParam,json,fetchAPI,deleteType,originalvmCount,apiUsername,apiPassword,safeType)
                  
                          println("Machines deleted so far: " + newvmCount)
                          if (newvmCount >= originalvmCount){
                               println("Machines have now been deleted")
                          }
                          
               else{
                     println('================================================================= ')
                    println("Continuing to now delete the slaves with connection times over 900ms (15 minutes)")
                     println('================================================================= ')
                                  slaveList=[]
          
          
               
                    for (aSlave in hudson.model.Hudson.instance.slaves) {
                   
                    if (aSlave.getComputer().getConnectTime() > 1000) {
                         slaveList.add(aSlave.name)
                }
                    }
                 newvmCount = deleteNode(excludeArray,jsonName,jsonDeleteParam,json,fetchAPI,deleteType,originalvmCount,apiUsername,apiPassword,safeType)
                  
                          println("Machines deleted so far: " + newvmCount)
                          if (newvmCount >= originalvmCount){
                               println("Machines have now been deleted")
                          }
                          
               else{
                    println('!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!')
                    println("Could not find enough non-efficent machines")
                    println('!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!')
                    }
                }
          }
     }
          
                
               
           
      else if (deleteType == "label") {
           println('==================== Deletion Option of Label: '+ deleteLabel + ' Commencing ====================');
             if (deleteLabel == null){
                  println('No slave label has been entered please re-visit your job configuration');
             }
              
               for (aSlave in hudson.model.Hudson.instance.slaves) {
                if (aSlave.getLabelString().equals(deleteLabel)){
                  slaveList.add(aSlave.name)
                 }
            }
              deleteNode(excludeArray,jsonName,jsonDeleteParam,json,fetchAPI,deleteType,Integer.MAX_VALUE,apiUsername,apiPassword,safeType);      
                 
     }
           println("=====================================================") 
        
            return  deletionMap;
         }
     
   }
   

