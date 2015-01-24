/*
 * created on 2005/1/4
 * 
 * $Author$ $Revision$ $Date$
 */
package io.kaif.mail;

import org.springframework.mail.SimpleMailMessage;

public class Mail extends SimpleMailMessage {

  private String fromName;

  public Mail() {
  }

  /**
   * equals() is same as {@link org.springframework.mail.SimpleMailMessage}. Note Mail's fromName
   * don't take affect on equals
   */
  @Override
  public boolean equals(Object other) {
    return super.equals(other);
  }

  /**
   * hashCode() is same as {@link org.springframework.mail.SimpleMailMessage}. Note Mail's fromName
   * don't take affect on hashCode
   */
  @Override
  public int hashCode() {
    return super.hashCode();
  }

  @Override
  public String toString() {
    return "[From person: " + fromName + " ]\n" + super.toString();
  }

  public String getFromName() {
    return fromName;
  }

  public void setFromName(String fromName) {
    this.fromName = fromName;
  }
}