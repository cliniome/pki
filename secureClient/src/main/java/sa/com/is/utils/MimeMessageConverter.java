package sa.com.is.utils;

import android.content.Context;
import android.util.Log;

import org.apache.james.mime4j.codec.QuotedPrintableOutputStream;
import org.apache.james.mime4j.util.MimeUtil;

import java.io.ByteArrayOutputStream;
import java.io.OutputStream;
import java.util.Set;

import sa.com.is.Address;
import sa.com.is.Body;
import sa.com.is.Message;
import sa.com.is.MessagingException;
import sa.com.is.Multipart;
import sa.com.is.filter.Base64;
import sa.com.is.internet.MimeBodyPart;
import sa.com.is.internet.MimeMessage;
import sa.com.is.internet.MimeMessageHelper;
import sa.com.is.internet.MimeMultipart;
import sa.com.is.internet.MimeUtility;
import sa.com.is.internet.TextBody;
import sa.com.is.mailstore.BinaryMemoryBody;
import sa.com.is.message.MessageBuilder;

/**
 * Created by snouto on 02/09/15.
 */
public class MimeMessageConverter {





    public Message signThatMessage(Message message,Context context)
    {
        try
        {
            if(message != null &&message.isSigned())
            {
                MimeMessage msg = new MimeMessage();

                msg.setEncrypted(message.isEncrypted());
                msg.setMessageId(message.getMessageId());
                msg.setSubject(message.getSubject());
                msg.setReplyTo(message.getReplyTo());

                msg.setServerExtra(message.getServerExtra());

                //copy all headers
                msg.setFrom(message.getFrom()[0]);

                //copy all reciepents
                addRecepients(message, msg);
                //copy headers
                copyHeaders(message, msg);

                //msg.setBody(message.getBody());
                ByteArrayOutputStream messageContents = new ByteArrayOutputStream();

                ByteArrayOutputStream bodybinary = new ByteArrayOutputStream();

                //OutputStream encodedOs = javax.mail.internet.MimeUtility.encode(bodybinary,MimeUtil.ENC_QUOTED_PRINTABLE);

                message.getBody().writeTo(messageContents);

              //  encodedOs.write(messageContents.toByteArray());





                MimeMessageHelper.setBody(msg, new TextBody(bodybinary.toString()));



                if(MimeUtil.isQuotedPrintableEncoded(bodybinary.toString()))
                {
                    Log.i("MimeMessageConverter","Yes it is quoted-printable");
                }else
                {
                    Log.i("MimeMessageConverter","No it is Not quoted-printable");
                }

                //Create a MimeMultipart of the message body
                MimeMultipart msgBody = new MimeMultipart();
                msgBody.setSubType("signed");

                //Creating a signed MimePart
                MimeBodyPart part = new MimeBodyPart();
                //name="smime.p7s"
                part.addHeader("Content-Type","application/pkcs7-signature;name=\"smime.p7s\"");
                part.addHeader("Content-Transfer-Encoding", "base64");
                //filename="smime.p7s"
                part.addHeader("Content-Disposition", "attachment;filename=\"smime.p7s\"");
                part.addHeader("Content-Description", "S/MIME Cryptographic Signature");


                //signed Body part


                msgBody.addBodyPart(new MimeBodyPart(msg.getBody()));



                Body signedBody = new BinaryMemoryBody(EncryptionManager.signMessage(context, bodybinary.toByteArray()),"UTF-8");
                //part.setBody(signedBody);

                MimeMessageHelper.setBody(part,signedBody);


                msgBody.addBodyPart(part);




                MimeMessageHelper.setBody(msg,msgBody);





                //finally return the message

                return msg;

            }else throw new Exception("Message can't be null");

        }catch (Exception s)
        {
            Log.e("Signing",s.getMessage());
            return null;
        }
    }

    private void copyHeaders(Message source, MimeMessage target) throws Exception{

        Set<String> headerNames = source.getHeaderNames();

        if(headerNames != null && !headerNames.isEmpty())
        {
            for(String headerName :headerNames)
            {
                String[] headerValues = source.getHeader(headerName);

                if(headerValues!= null && headerValues.length >0)
                {
                    for(String headerValue :headerValues)
                    {
                        target.setHeader(headerName,headerValue);
                    }
                }
            }
        }


    }

    private void addRecepients(Message source, MimeMessage target) throws Exception {

        Address[] TO = source.getRecipients(Message.RecipientType.TO);

        if(TO!= null &&TO.length >0)
        {
            target.setRecipients(Message.RecipientType.TO,TO);
        }

        //add CC
        Address[] CC = source.getRecipients(Message.RecipientType.CC);

        if(CC!=null &&CC.length >0)
        {
            target.setRecipients(Message.RecipientType.CC,CC);
        }

        //Add BCC
        Address[] BCC = source.getRecipients(Message.RecipientType.BCC);

        if(BCC!= null &&BCC.length >0)
        {
            target.setRecipients(Message.RecipientType.BCC,BCC);
        }

    }

}
