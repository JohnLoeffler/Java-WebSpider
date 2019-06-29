package com.loeffler.webspider.sitemap.siteobjects;
import com.loeffler.utilitylibrary.Logging.Logger;
import com.loeffler.utilitylibrary.Statics;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 *  <p><strong>SiteMap</strong></p>
 *  <em>@author</em>  John Loeffler
 *  
 *  <strong>Contact</strong> 
 *    <em>@Email</em>     John.Loeffler@gmail.com
 *    <em>@Twitter</em>   @ThisDotJohn
 *    <em>@LinkedIn</em>  LinkedIn.com/in/JohnLoeffler
 *    <em>@Github</em>    github.com/JohnLoeffler
 *    <em>@Website</em>   JohnLoeffler.com
 */
public class SiteMap {
  protected static Logger LOG = Logger.GetInstance();
  protected Map<Integer, PageData>  WebsiteMap;
  protected int                     NextID;
  protected long                    NumHREFS;
  
  /* Standard methods like constructors, destructor, setters, and getters */
  /**
   *  Default Constructor, creates ConcurrentHashMap,sets NextID,NumHREFS to '0' 
   */
  public SiteMap(){
    WebsiteMap = new ConcurrentHashMap<>();
    NextID = 0;
    NumHREFS = 0;
  }
  /**
   * Gets the number of the ID that will be assigned to the next PageData added
   * @return An int of the next ID to be assigned
   */
  public int  getNextID() {return NextID;}
  /**
   * Gets the sum of all HREF links contained in PageData Hyperlink Lists
   * @return A long of the sum of the PageData's HREF Lists sizes
   */
  public long getNumHREFS() {return NumHREFS;}
  
  
  public int  addPage(PageData page){
    try{
      Integer i = NextID;
      page.PageID = NextID;
      NextID++;
      this.WebsiteMap.put(i, page);
      return 0;
    }catch(Exception ex){
      // TODO Write up a log message to explain what went wrong here
      return 1;
    }
  }
  /**
   *  Gets the size of the SiteMap
   *  @return An int of how many pages are in the SiteMap
   */
  public int  getSize(){return WebsiteMap.size();}
  /**
   *  Prints the SiteMap as a .CSV file
   *  @param file filename to save SiteMap to. Don't need to add .csv
   *  @param append True if method should append data to file, false overwrites
   *  @return The number of total lines printed to file
   */
  public int  printSiteMap(String file, boolean append){
    int lines = 0;
    File outfile = null;
    if(file != null){
      if(!file.equals("")){
        outfile = new File(String.format("%s.csv", file));
      }else{
        outfile = new File("SiteMap.csv");
      }
    }else{
      outfile = new File("SiteMap.csv");
    }
    try(BufferedWriter bw = new BufferedWriter(new FileWriter(outfile,append))){
      if(!append){
        bw.write("SOURCE|TARGET|ALT_TEXT|ANCHOR|RESPONSE_CODE|STATUS|DO_FOLLOW");
        bw.newLine();
      }
      StringBuilder sb = new StringBuilder();
      for(int i =0; i < WebsiteMap.size(); i++){
        try{
          PageData pd = ((PageData[]) WebsiteMap.values().toArray())[i];
          lines += pd.HRefs.size();
          bw.write(pd.toString());
        }catch(Exception ex){
          LOG.Log(Statics.Class(), Statics.Method(), Statics.Line(), 
            String.format("Threw Exception trying to converting HashMap value"
            + " to PageData"), 1);
        }
      }
    }catch(IOException ioe){
      //  TODO Add a log message to this IOException 
      return -1;
    }
    return lines;
  }
}
