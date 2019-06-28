
package com.loeffler.webspider.sitemap.siteobjects;
import java.util.ArrayList;
import java.util.List;

/**
 *  <p><strong>PageData</strong></p>
 *  <em>@author</em>  John Loeffler
 *  
 *  <strong>Contact</strong> 
 *    <em>@Email</em>     John.Loeffler@gmail.com
 *    <em>@Twitter</em>   @ThisDotJohn
 *    <em>@LinkedIn</em>  LinkedIn.com/in/JohnLoeffler
 *    <em>@Github</em>    github.com/JohnLoeffler
 *    <em>@Website</em>   JohnLoeffler.com
 */
public class PageData {
  public String           Url;
  public List<Hyperlink>  HRefs;
  public int              PageID;
  /**
   * Parameterized Constructor
   * @param Url       The URL of the Webpage
   * @param HRefs     The Internal Links on the Webpage
   * @param PageID    The PageID of the Webpage
   */
  public PageData(String Url, List<Hyperlink> HRefs, int PageID) {
    this.Url    = Url;
    this.HRefs  = new ArrayList<>();
    if(HRefs != null){
      this.HRefs.addAll(HRefs);
    }
    this.PageID = PageID;
  }
  /**
   * Default Constructor
   */
  public PageData(){}
  /**
   * Returns the PageData containing the href data as a single string, with 
   *  component parts delimited by a "|" and each link delimited by a "\t"
   * @return The String of the PageData
   */
  public String toString() {
    StringBuilder sb = new StringBuilder();
    for(Hyperlink h : HRefs){
      sb.append(Url).append("|").append(h.toString()).append('\n');
    }
    return sb.toString();
  }
}
