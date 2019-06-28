
package com.loeffler.webspider;

import com.loeffler.utilitylibrary.Logging.Logger;
import com.loeffler.utilitylibrary.Statics;
import com.loeffler.webspider.sitemap.SiteMapSpider;

/**
 *  <p><strong>DemonstrationRunner</strong></p>
 *  <em>@author</em>  John Loeffler
 *  
 *  <strong>Contact</strong> 
 *    <em>@Email</em>     John.Loeffler@gmail.com
 *    <em>@Twitter</em>   @ThisDotJohn
 *    <em>@LinkedIn</em>  LinkedIn.com/in/JohnLoeffler
 *    <em>@Github</em>    github.com/JohnLoeffler
 *    <em>@Bitbucket</em> bitbucket.org/JohnLoeffler
 */
public class DemonstrationRunner {
  public static Logger LOG = Logger.GetInstance();
  
  /**
   * @param args the command line arguments
   */
  public static void main(String[] args) {
    SiteMapSpider sms = new SiteMapSpider();
    sms.Init(Integer.parseInt(args[0]), Integer.parseInt(args[1]), 
      Integer.parseInt(args[2]), args[3]);
    try {
      if(sms == null){
        System.out.println("Spider is null");
      }
      sms.BeginCrawl("https://www.interestingengineering.com/");
    } catch (Exception e) {
      LOG.Log(Statics.Class(), Statics.Method(), Statics.Line(), String.format(
        "Exception thrown up to main(): %s", e.getMessage()), 4);
    }
    sms.GetSiteMap().printSiteMap("SiteMapAlpha", false);
    System.exit(0);
  }
}
