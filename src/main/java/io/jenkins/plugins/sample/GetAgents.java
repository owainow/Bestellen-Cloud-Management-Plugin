/*
 * The MIT License
 *
 * Copyright 2020 Owain Osborne-Walsh/tigerbaylimited.
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
package io.jenkins.plugins.sample;


import groovy.lang.GroovyClassLoader;
import groovy.lang.GroovyObject;
import java.io.File;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 *
 * @author tigerbaylimited
 */

public class GetAgents{
///Users/tigerbaylimited/Documents/Final_Year/Bestellen/plugindev/Bestellen/work/nodes
    
    public String result;
    public GroovyClassLoader classLoader;
    public String exclude;
    public String acloudType ;
    public String deleteType;
    public String adeleteLabel;
    public String avmCount;
    public String afetchAPI;
    public String ajsonName;
    public String ajsonDeleteParam;
    public String aapiUsername;
    public String aapiPassword;
    public LinkedHashMap deletionMap;
    public String deletionMapString;
    public List<String> returnArray = new ArrayList<>();
    public String awsID;
    public String awsKey;
    public String awsRegion;

    
   
    public GetAgents(String Exclude,String CloudType, String GoalType,String deleteLabel,String vmCount,String fetchAPI,String apiUsername,String apiPassword,String jsonName,String jsonDeleteParam,String awsID,String awsKey,String awsRegion)  {
        this.exclude = Exclude;
        this.acloudType=CloudType;
        this.deleteType=GoalType;
        this.adeleteLabel=deleteLabel;
        this.avmCount=vmCount;
        this.afetchAPI=fetchAPI;
        this.ajsonName=jsonName;
        this.ajsonDeleteParam=jsonDeleteParam;
        this.aapiUsername=apiUsername;
        this.aapiPassword=apiPassword;
        this.awsID=awsID;
        this.awsKey=awsKey;
        this.awsRegion=awsRegion;
    }

    
    public List<String> setnodelocation (String args)throws Exception {
        //Priviliged action in order to guarantee that field.setAccessible(true) will always be granted permission
    AccessController.doPrivileged((PrivilegedAction) () -> { 
    classLoader = new GroovyClassLoader();
    return classLoader;
});


Class groovy = classLoader.parseClass(new File("src/main/java/io/jenkins/plugins/sample/getNodes.groovy"));
GroovyObject groovyObj = (GroovyObject) groovy.getDeclaredConstructor().newInstance();

ByteArrayOutputStream buffer = new ByteArrayOutputStream()  ;

PrintStream saveSystemOut = System.out ;
final String utf8 = StandardCharsets.UTF_8.name();
System.setOut( new PrintStream ( buffer,true, utf8 )) ;

String[] paramArguments = {exclude,acloudType,deleteType,adeleteLabel,avmCount,afetchAPI,aapiUsername,aapiPassword,ajsonName,ajsonDeleteParam,awsID,awsKey,awsRegion};
Object call = groovyObj.invokeMethod("loopnodes", paramArguments);
deletionMap = (LinkedHashMap)call;
System.setOut( saveSystemOut ) ; 
deletionMapString = deletionMap.toString();
result = buffer.toString( "UTF-8" ).trim() ;
returnArray.add(result);
returnArray.add(deletionMapString);
       return returnArray;
    }
}
