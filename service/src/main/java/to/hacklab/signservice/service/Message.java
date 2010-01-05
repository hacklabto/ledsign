package to.hacklab.signservice.service;

public class Message
{
    public enum BorderType
    {
        None,
        Thin,
        Thick,
        Marquee
    }

    private String text;

    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        this.text = text;
    }

    private String fontName;

    public String getFontName()
    {
        return fontName;
    }

    public void setFontName(String fontName)
    {
        this.fontName = fontName;
    }

    private int fontSize;

    public int getFontSize()
    {
        return fontSize;
    }

    public void setFontSize(int fontSize)
    {
        this.fontSize = fontSize;
    }

    private BorderType borderType;

    public BorderType getBorderType()
    {
        return borderType;
    }

    public void setBorderType(BorderType borderType)
    {
       this.borderType = borderType;
    }

    public Message(String text, String fontName, int fontSize, BorderType borderType)
    {
        this.text = text;
        this.fontName = fontName;
        this.fontSize = fontSize;
        this.borderType = borderType;
    }
}
