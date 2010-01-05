package to.hacklab.signservice.action;

import com.polarrose.wsf.WebServiceAccount;
import com.polarrose.wsf.action.ActionHandlerException;
import com.polarrose.wsf.action.PublicWebServiceActionHandler;
import com.polarrose.wsf.action.WebServiceActionContext;
import com.polarrose.wsf.annotation.WebServiceParameter;
import com.polarrose.wsf.stereotype.WebServiceAction;
import org.springframework.beans.factory.annotation.Autowired;
import to.hacklab.signservice.service.Message;
import to.hacklab.signservice.service.SignService;

@WebServiceAction(name = "ShowMessage", version = "2009-02-03")
public class ShowMessageAction extends PublicWebServiceActionHandler<ShowMessageAction.Parameters>
{
    private SignService signService;

    @Autowired
    public void setSignService(SignService signService)
    {
        this.signService = signService;
    }

    public Object execute(WebServiceActionContext<WebServiceAccount> context, Parameters parameters) throws ActionHandlerException
    {
        Message message = new Message(
            parameters.getMessage(),
            parameters.getFontName(),
            parameters.getFontSize(),
            parameters.getBorderType()
        );

        signService.queueMessage(message);

        return null;
    }

    public static class Parameters
    {
        private String message;

        public String getMessage()
        {
            return message;
        }

        @WebServiceParameter
        public void setMessage(String message)
        {
            this.message = message;
        }

        private String fontName = "SansSerif";

        public String getFontName()
        {
            return fontName;
        }

        @WebServiceParameter(optional = true)
        public void setFontName(String fontName)
        {
            this.fontName = fontName;
        }

        private int fontSize = 12;

        public int getFontSize()
        {
            return fontSize;
        }

        @WebServiceParameter(optional = true)
        public void setFontSize(int fontSize)
        {
            this.fontSize = fontSize;
        }

        private Message.BorderType borderType = Message.BorderType.None;

        public Message.BorderType getBorderType()
        {
            return borderType;
        }

        @WebServiceParameter(optional = true)
        public void setBorderType(Message.BorderType borderType)
        {
            this.borderType = borderType;
        }
    }
}