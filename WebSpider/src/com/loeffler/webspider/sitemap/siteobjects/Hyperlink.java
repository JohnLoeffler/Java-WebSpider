
package com.loeffler.webspider.sitemap.siteobjects;

/**
 *  <p><strong>Hyperlink</strong></p>
 *  <em>@author</em>  John Loeffler
 *  
 *  <strong>Contact</strong> 
 *    <em>@Email</em>     John.Loeffler@gmail.com
 *    <em>@Twitter</em>   @ThisDotJohn
 *    <em>@LinkedIn</em>  LinkedIn.com/in/JohnLoeffler
 *    <em>@Github</em>    github.com/JohnLoeffler
 *    <em>@Website</em>   JohnLoeffler.com
 */
public class Hyperlink {
  /* DATA MEMBERS */
  public  String  Url, AltText, Anchor;
  public  int     Status;
  public  boolean bFollow;
  
  /* MEMBER METHODS */
  /**
   * Default constructor
   */
  public Hyperlink(){
    this.Url    = "";
    this.AltText= "";
    this.Anchor = "";
    this.Status = 0;
    this.bFollow= false;
  }
  /**
   * Parameterized constructor
   * @param u   A String of the URL for the href
   * @param at  A String of the Alt-Text for the href
   * @param ah  A String of the Anchor for the href
   * @param st  An int of the connection status of the href
   * @param bf  A Boolean indicating whether the link is DOFOLLOW or not
   */
  public Hyperlink(String u, String at, String ah, int st, boolean bf){
    this.Url    = u;
    this.AltText= at;
    this.Anchor = ah;
    this.Status = st;
    this.bFollow= bf;}
  /**
   * Produces a String of the hyperlink in .CSV format
   * @return The string of the hyperlink information
   */
  @Override
  final public String toString(){
    StringBuilder sb = new StringBuilder();
    sb.append(this.Url);
    sb.append("|");
    sb.append(this.AltText);
    sb.append("|");
    sb.append(this.Anchor);
    sb.append("|");
    sb.append(this.Status);
    sb.append("|");
    if(this.Status == 200){
      sb.append("OKAY|");
    }else{
      sb.append("ERROR|");
    }
    if(bFollow){
      sb.append("TRUE");
    }else{
      sb.append("FALSE");
    }
    return sb.toString();
  }
}
