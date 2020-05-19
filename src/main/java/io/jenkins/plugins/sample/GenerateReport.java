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
package io.jenkins.plugins.sample;

import com.itextpdf.awt.DefaultFontMapper;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.BaseColor;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Element;
import com.itextpdf.text.FontFactory;
import com.itextpdf.text.Image;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.pdf.CMYKColor;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfTemplate;
import com.itextpdf.text.pdf.PdfWriter;
import hudson.FilePath;
import io.jenkins.cli.shaded.org.apache.commons.lang.ArrayUtils;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import static java.sql.DriverManager.println;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import org.jfree.chart.JFreeChart; 
import org.jfree.chart.ChartFactory; 
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
/**
 *
 * @author tigerbaylimited
 */
public class GenerateReport {
    public String exclude;
    final String utf8 = StandardCharsets.UTF_8.name();
    public String acloudType ;
    public String deleteType;
    public String adeleteLabel;
    public String avmCount;
    public String afetchAPI;
    public String ajsonName;
    public String ajsonDeleteParam;
    public String adeleteVMString;
    public String title;
    public int deleteNumber = 0;
    public int previousdeleteNumber;
    public int deleteDifference;
    public FilePath workspace;
 
 


  public GenerateReport(String Exclude,String CloudType, String GoalType,String deleteLabel,String vmCount,String fetchAPI,String jsonName,String jsonDeleteParam,String deleteVMString,FilePath workspace){
        this.exclude = Exclude.toUpperCase();
        this.acloudType=CloudType.toUpperCase();
        this.deleteType=GoalType.toUpperCase();
        this.adeleteLabel=deleteLabel.toUpperCase();
        this.avmCount=vmCount.toUpperCase();
        this.afetchAPI=fetchAPI.toUpperCase();
        this.ajsonName=jsonName.toUpperCase();
        this.ajsonDeleteParam=jsonDeleteParam.toUpperCase();
        try{
        this.adeleteVMString=deleteVMString.toUpperCase();
        }
        catch(Exception e){
            println("Null pointer exception something has gone wrong in the groovy scripts");
        }
        this.workspace=workspace;
        
}
   public String runReport(String args) throws BadElementException, IOException, DocumentException {
        GenerateReport programm = new GenerateReport(exclude,acloudType,deleteType,adeleteLabel,avmCount,afetchAPI,ajsonName,ajsonDeleteParam,adeleteVMString,workspace);
        args = programm.start();
        return args;
               
    }


 public String start() throws BadElementException, IOException, DocumentException {
       String workspaceString = workspace.toString();
       
  File newReportDirectory = new File (workspaceString+"/latestReport"); // Setting workspace file locations created in createReportFolders.groovy
  File previousReportDirectory = new File(workspaceString+"/previousReports/");
   File[] content = newReportDirectory.listFiles();
if(newReportDirectory.isDirectory()) {
           for (File content1 : content) {
               boolean success = content1.renameTo(new File(previousReportDirectory, content1.getName())); //Rename moves all files from latest to previous
               if (!success) {
                   System.out.print("Failed moving files");
               }
           }
}
       
       
       
       Document document = new Document();
       LocalDateTime runDateTime = LocalDateTime.now();
       DateTimeFormatter formatting = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
       String formattedDate = runDateTime.format(formatting);
  
        title = "Bestellen_"+formattedDate;
       String FILE_NAME = newReportDirectory+"/"+title;
      
       
      adeleteVMString  = adeleteVMString.substring(1, adeleteVMString.length()-1); //Remove brackets from string.
       String[] keyValuePairs = adeleteVMString.split(",");              //split the string to creat key-value pairs
       Map<String,String> machines = new HashMap<>();               
        
       for(String pair : keyValuePairs)                        //iterate over the pairs
     {
       try{
    String[] entry = pair.split("=");                   //split the pairs to get key and value 
    machines.put(entry[0].trim(), entry[1].trim());    
       }                                   //add them to the hashmap and trim whitespaces
       catch (Exception e) {
      System.out.println("It would appear keyValuePairs is empty. This is most likley as no machines were deleted.");
    }
       }
    File deleteFile = new File("machinesDeleted.txt"); //DEPRECATED GOING TO REPLACE with the below file
   boolean createDeleteFile = deleteFile.createNewFile();
    if (!createDeleteFile) {
    println("Document machinesDeleted.txt already exists.");
     }
    
     File previousFile = new File("previousDelete.txt");
   boolean createPreviousFile= previousFile.createNewFile();
    if (!createPreviousFile) {
    println("Document previousDelete.txt already exists.");
     }
  
    
    Scanner deleteReader = new Scanner(deleteFile,utf8);
      while (deleteReader.hasNextLine()) {
        previousdeleteNumber = Integer.parseInt(deleteReader.nextLine());
     }
      deleteReader.close();
      
   
       Scanner previousReader = new Scanner(previousFile,utf8);
        List<Integer> previousDeletes = new ArrayList<>();
      while (previousReader.hasNext()) {
              if (previousReader.hasNextInt()) {
            previousDeletes.add(previousReader.nextInt());
     }
              else {
            previousReader.next();
        }
      }
       previousReader.close();
       
    
       
      
      try //Main body of report
      {
         PdfWriter writer =  PdfWriter.getInstance(document, new FileOutputStream(new File(FILE_NAME)));
         document.open();
         
        com.itextpdf.text.Font heading1 = FontFactory.getFont(FontFactory.HELVETICA, 18, Font.BOLD, new CMYKColor(60, 40, 40, 100));
        com.itextpdf.text.Font heading2 = FontFactory.getFont(FontFactory.HELVETICA, 14, Font.PLAIN, new CMYKColor(60, 40, 40, 100));
        com.itextpdf.text.Font heading3 = FontFactory.getFont(FontFactory.HELVETICA, 10, Font.PLAIN, new CMYKColor(60, 40, 40, 100));
         //Add attributes to PDF.
           document.addAuthor("Bestellen - The Jenkins Cloud Plugin");
           document.addCreationDate();
           document.addCreator("Owain Osborne-Walsh");
           document.addTitle("Bestellen_"+formattedDate);
           title = "Bestellen_"+formattedDate;
           document.addSubject("Bestellen run for "+acloudType+ ". With:" + exclude + " excluded. Set to deletion type: "+ deleteType);
           
           
           //Add Bestellen Logo to PDF
           //Add Image
            String imageUrl = "https://i.ibb.co/p0DJrLf/Bestellen-Logo.png";
            Image logo = Image.getInstance(new URL(imageUrl));
            //Scale image.
            logo.scaleAbsolute(500, 120);
           //Add to document
           document.add(logo);
         Paragraph paragraphOne =  new Paragraph("Welcome to your Bestellen report. "+formattedDate,heading1);
         document.add(paragraphOne);
         Paragraph paragraphTwo = new Paragraph("\nRun Settings:",heading1);
          document.add(paragraphTwo);
         Paragraph paragraphThree = new Paragraph("Bestellen run for "+acloudType+ ". With Machines: " + exclude + " excluded. Set to deletion type: "+ deleteType + ".",heading3);
         document.add(paragraphThree);
        Paragraph paragraphFour= new Paragraph("\nDeleted Machines",heading1);
        document.add(paragraphFour);
        PdfPTable table = new PdfPTable(2); // 2 columns.
        table.setWidthPercentage(100); //Width 100%
        table.setSpacingBefore(15f); //Space before table
        table.setSpacingAfter(25f); //Space after table
 
     
        if (ArrayUtils.isEmpty(keyValuePairs)){
             Paragraph paragraphDelete= new Paragraph("\nIt would appear that no machines have been deleted in this run. Check console if you believe this is incorrect.",heading1);
        document.add(paragraphDelete);
        }
        else{
            
        
        float[] columnWidths = {1f, 1f};
        table.setWidths(columnWidths);
 
        PdfPCell cell1 = new PdfPCell(new Paragraph("Machine Name"));
        cell1.setBorderColor(BaseColor.BLACK);
        cell1.setPaddingLeft(10);
        cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell1.setVerticalAlignment(Element.ALIGN_MIDDLE);
 
        PdfPCell cell2 = new PdfPCell(new Paragraph("Machine ID"));
        cell2.setBorderColor(BaseColor.DARK_GRAY);
        cell2.setPaddingLeft(10);
        cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell2.setVerticalAlignment(Element.ALIGN_MIDDLE);
 
 
        table.addCell(cell1);
        table.addCell(cell2);
     
 

        
       for (Map.Entry<String, String> entry : machines.entrySet()) { //For each machine that has been passed through as deleted.
           deleteNumber++;
         
           PdfPCell cellName = new PdfPCell(new Paragraph(entry.getKey()));   // Creating cells
           PdfPCell cellID = new PdfPCell(new Paragraph(entry.getValue()));

           table.addCell(cellName);
           table.addCell(cellID); 
      }
         document.add(table);
         
       
         
           if (previousDeletes.size() >= 10){
         
         previousDeletes.remove(previousDeletes.size() - 1 ); //Ensuring that the deletion file does not exceed 10 to ensure easy readability.
         }
         previousDeletes.add(0,deleteNumber); //Add latest run at the top of the file.
       PrintWriter writeNew = new PrintWriter("previousDelete.txt", "UTF-8");
       
        for (int number : previousDeletes) {
             
	   writeNew.println( String.valueOf(number));
                      }
        
        writeNew.close();
         
        } 
        
         
          Paragraph paragraphFive= new Paragraph("\nMachines deleted total: "+deleteNumber,heading2);
        document.add(paragraphFive);
         if( previousdeleteNumber == 0){
            deleteDifference = deleteNumber-previousdeleteNumber;
        Paragraph paragraphSix= new Paragraph("\nThis is your first run generating a report. Congratulations! Next time I will tell you how many more/less machines have been deleted in the current run.",heading2);
         document.add(paragraphSix);
           }
       else if(deleteNumber > previousdeleteNumber){
            deleteDifference = deleteNumber-previousdeleteNumber;
        Paragraph paragraphSix= new Paragraph("\nThis is "+ deleteDifference + " more then your previous run.",heading2);
         document.add(paragraphSix);
           }
        
         else if (deleteNumber < previousdeleteNumber)
        {
                    deleteDifference = previousdeleteNumber-deleteNumber;
                 Paragraph paragraphSeven= new Paragraph("\nThis is "+ deleteDifference + " less then your previous run.",heading2);
                  document.add(paragraphSeven);
                }
         else{
                Paragraph paragraphEight= new Paragraph("\nThis run deleted the same amount of machines as the previous run.",heading2);
                  document.add(paragraphEight);
         }
         
        if(!previousDeletes.isEmpty() && previousDeletes.size() > 1){ //Generate chart if more then one previous run in the text file
                         
            int width = 640; //Set Width
            int height=480; //Set Height
            int i = 1;
          
           final XYSeries deleted = new XYSeries( "Machines" );
           for (int temp : previousDeletes) {
             
	    deleted.add(i,temp);
            i++;
                      }
  
      final XYSeriesCollection dataset = new XYSeriesCollection( );
      dataset.addSeries( deleted );
 

      JFreeChart xylineChart = ChartFactory.createXYLineChart(
         "Machines Deleted in 10 latest runs.", 
         "Most Recent -> Least Recent runs.",
         "Number of Machines Deleted", 
         dataset,
         PlotOrientation.VERTICAL, 
         true, true, false);
        
          /* Width of the image */
        

        PdfContentByte Add_Chart_Content = writer.getDirectContent();
        PdfTemplate template_Chart_Holder = Add_Chart_Content
                .createTemplate(width, height);
        Graphics2D Graphics_Chart = template_Chart_Holder.createGraphics(
                width, height, new DefaultFontMapper());
              Rectangle2D Chart_Region = new Rectangle2D.Double(0, 0, 540, 380);
         
        xylineChart.draw(Graphics_Chart, Chart_Region);
        Graphics_Chart.dispose();
     //   Add_Chart_Content.addTemplate(template_Chart_Holder, 0, 0);
        Image chartImage = Image.getInstance(template_Chart_Holder);
        document.add(chartImage);
        } 
        else{
                    Paragraph paragraphGraph= new Paragraph("\n Once we have more run data a graph will be here showing your last 10 runs!",heading2);
         document.add(paragraphGraph);
        }
         document.close();
         writer.close();
      
         
      } 
      catch (DocumentException | FileNotFoundException e)
      {
         
      }
        
     PrintWriter writer = new PrintWriter("machinesDeleted.txt", "UTF-8");
       writer.println( String.valueOf(deleteNumber) );
        writer.close();
            return title;
   }
}
  
    

   