package to.hacklab.signservice.action;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

import com.polarrose.wsf.WebServiceAccount;
import com.polarrose.wsf.stereotype.WebServiceAction;
import com.polarrose.wsf.action.ActionHandlerException;
import com.polarrose.wsf.action.PublicWebServiceActionHandler;
import com.polarrose.wsf.action.WebServiceActionContext;

@WebServiceAction(name = "GetAvailableFonts", version = "2009-02-03")
public class GetAvailableFontsAction extends PublicWebServiceActionHandler<GetAvailableFontsAction.Parameters>
{
    public List<String> execute(WebServiceActionContext<WebServiceAccount> context, Parameters parameters) throws ActionHandlerException
    {
        List<String> fontNames = new ArrayList<String>();

        GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();

        for (Font font : environment.getAllFonts()) {
            fontNames.add(font.getFontName());
        }

        return fontNames;
    }

    public static class Parameters
    {
    }
}
