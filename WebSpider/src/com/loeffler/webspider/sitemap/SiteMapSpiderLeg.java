
package com.loeffler.webspider.sitemap;
import com.loeffler.utilitylibrary.Statics;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.jsoup.Connection;
import org.jsoup.HttpStatusException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import com.loeffler.webspider.WebSpiderLeg;
import com.loeffler.webspider.WebSpider;
import com.loeffler.webspider.sitemap.siteobjects.Hyperlink;
import com.loeffler.webspider.sitemap.siteobjects.PageData;

/**
 *  <p><strong>SiteMapSpiderLeg</strong></p>
 *  <em>@author</em>  John Loeffler
 *  
 *  <strong>Contact</strong> 
 *    <em>@Email</em>     John.Loeffler@gmail.com
 *    <em>@Twitter</em>   @ThisDotJohn
 *    <em>@LinkedIn</em>  LinkedIn.com/in/JohnLoeffler
 *    <em>@Github</em>    github.com/JohnLoeffler
 *    <em>@Website</em>   JohnLoeffler.com
 */
public class SiteMapSpiderLeg extends WebSpiderLeg{
  private PageData  PageData;
  private String    DomainUrl;
  protected static Map<String, Integer> ResponseCodes=new ConcurrentHashMap<>();
  
  public SiteMapSpiderLeg(String url, String domain, WebSpider body){
    super(url, body);
    this.PageData = new PageData();
    this.DomainUrl= domain;
    
  }
  /**
   * Gets the html document from the SiteMapSpiderLeg's url
   * @return An HTML Document
   */
  private Document GetHTMLDocumentFromURL(){
    if(this.Url == null){
      LOG.Log(Statics.Class(), Statics.Method(), Statics.Line(), String.format(
        "Error, SiteMapSpiderLeg cannot process a 'null' url!"), 3);
      return null;
    }
    
    Document document = null;
    Connection connection = null;
    
    try {
      //  Connect to URL
      connection = Jsoup.connect(Url).userAgent(USER_AGENT);
      try {
        document = connection.get();
        ResponseCodes.put(Url, 200);
        return document;
      } catch (HttpStatusException hse) {
        if(!ResponseCodes.containsKey(Url)){
          Integer code = connection.ignoreHttpErrors(true)
                                      .userAgent(USER_AGENT)
                                      .execute().statusCode();
          ResponseCodes.put(Url, code);
        }
        return null;
      }
    } catch (Exception ioe) {
      LOG.Log(Statics.Class(), Statics.Method(), Statics.Line(), String.format(
        "Exception thrown while attempting to make Connection to url: %s", 
        ioe.getMessage()), 4);
      return null;
    }
  }
  
  private List<Hyperlink> GetInternalLinks(Document document){
    List<Hyperlink> links = new ArrayList<>();
    Elements elems = null;
    try{
      elems = document.select(String.format("a[href*=%s]", DomainUrl));
    }catch(NullPointerException npe){
      return links;
    }
    
    for(Element e : elems){
      String  url     = e.absUrl("href").toLowerCase().trim(),
              altText = e.getElementsByAttribute("alt").val(),
              anchor  = e.text(),
              follow  = e.getElementsByAttribute("rel").attr("rel");
      boolean bFollow = false;
      if(follow.equals("dofollow")){
        bFollow = true;
      }
      Integer res = GetURLResponseCode(url);
      links.add(new Hyperlink(url, altText, anchor, res, bFollow));
    }
    return links;
  }
  
  private Integer GetURLResponseCode(String url){
    if(ResponseCodes.containsKey(url)){
      return ResponseCodes.get(url);
    }
    Integer res = 200;
    try {
      res = Jsoup.connect(url)
              .ignoreHttpErrors(true)
              .userAgent(USER_AGENT)
              .execute()
              .statusCode();
      ResponseCodes.put(url, res);
    } catch (Exception e) {
      LOG.Log(Statics.Class(), Statics.Method(), Statics.Line(), String.format(
        "Exception thrown while attempting to get response code for: %s\n\t%s", 
          url, e.getMessage()), 3);
      return -1;
    }
    return res;
  }
  
  @Override
  public void run() {
    this.setActive(true);
    this.RunningTimer.Start();
    
    //  Get the HTML Document from the URL
    Document doc = GetHTMLDocumentFromURL();
    
    //  Process the Document
    ProcessURL(doc);
    
    try {
      this.GetBody().Notify(this);
    } catch (Exception e) {
      LOG.Log(Statics.Class(), Statics.Method(), Statics.Line(), String.format(
        "Exception thrown by SiteMapSpiderLeg processing %s; while attempting "
          + "to Notify Spider body of results, received: %s", 
          GetURL(), e.getMessage()), 3);
    }
    //  Final shutdown block to kill off the SiteMapSpiderLeg
    this.RunningTime = RunningTimer.Stop();
    this.setActive(false);
    this.setAlive(false);
  }
  
  @Override
  protected Object ProcessURL(Object o){
    Document doc = null;
    try {
      doc = (Document) o;
    } catch (Exception e) {
      LOG.Log(Statics.Class(), Statics.Method(), Statics.Line(), String.format(
        "Exception thrown while attempting to cast object to Document: %s", 
          e.getMessage()), 3);
    }
    try {
      if(doc != null){
        List<Hyperlink> links = GetInternalLinks(doc);
        this.PageData.Url   = this.Url;
        this.PageData.HRefs.addAll(links);
      }
    } catch (Exception e) {
      LOG.Log(Statics.Class(), Statics.Method(), Statics.Line(), String.format(
        "Exception thrown while attempting to populate PageData in Leg: %s", 
          e.getMessage()), 4);
    }
    return null;
  }

  @Override
  public Object ReportResults() {
    return this.PageData;
  }

}
