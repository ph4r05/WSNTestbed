/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package fi.wsnusbcollect.notify;

import java.util.Properties;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
* Simple demonstration of using the javax.mail API.
*
* Run from the command line. Please edit the implementation
* to use correct email addresses and host name.
*/
public class Emailer {
    private static final Logger log = LoggerFactory.getLogger(Emailer.class);    
    
    public static void sendMail(String to, String subject, String body){
      // Sender's email ID needs to be mentioned
      String from = "testbed@centaur.fi.muni.cz";

      // Assuming you are sending email from localhost
      String host = "localhost";

      // Get system properties
      Properties properties = System.getProperties();

      // Setup mail server
      properties.setProperty("mail.smtp.host", host);

      // Get the default Session object.
      Session session = Session.getDefaultInstance(properties);

      try{
         // Create a default MimeMessage object.
         MimeMessage message = new MimeMessage(session);

         // Set From: header field of the header.
         message.setFrom(new InternetAddress(from));

         // Set To: header field of the header.
         message.addRecipient(Message.RecipientType.TO,
                                  new InternetAddress(to));

         // Set Subject: header field
         message.setSubject(subject);

         // Now set the actual message
         message.setText(body);

         // Send message
         Transport.send(message);
         
         log.info("Sent message successfully....");
      }catch (MessagingException mex) {
          log.error("Cannot send mail, exception: ", mex);
      }
   }
} 

