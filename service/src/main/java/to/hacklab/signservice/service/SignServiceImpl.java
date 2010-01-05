package to.hacklab.signservice.service;

import java.awt.*;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import to.hacklab.movingSign.blinkenLights.BLFramePacket;
import to.hacklab.movingSign.blinkenLights.BLNetworkException;
import to.hacklab.movingSign.blinkenLights.BLPacketSender;

public class SignServiceImpl implements SignService, InitializingBean
{
    private String host;

    @Required
    public String getHost()
    {
        return host;
    }

    public void setHost(String host)
    {
        this.host = host;
    }

    private int port;

    @Required
    public int getPort()
    {
        return port;
    }

    public void setPort(int port)
    {
        this.port = port;
    }

    private BlockingQueue<Message> messageQueue = new LinkedBlockingQueue<Message>();

    public void queueMessage(Message message)
    {
        messageQueue.add(message);
    }

    private Thread messageThread;

    public void afterPropertiesSet() throws Exception
    {
        messageThread = new Thread(new MessageRunnable());
        messageThread.start();
    }

    class MessageRunnable implements Runnable
    {
        public void run()
        {
            BLPacketSender packetSender;

            try {
                packetSender = new BLPacketSender(host, port);
            } catch (BLNetworkException e) {
                throw new RuntimeException(e);
            }

            BufferedImage image = new BufferedImage(96, 32, BufferedImage.TYPE_INT_ARGB);
            Graphics2D graphics = (Graphics2D) image.getGraphics();

            while (true)
            {
                try {
                    Message message = messageQueue.take();

                    System.out.println("Text = " + message.getText() + " Font = " + message.getFontName() + " Size = " + message.getFontSize());

                    String[] lines = message.getText().split("\n");

                    Font font = new Font(message.getFontName(), Font.BOLD, message.getFontSize());
                    graphics.setFont(font);
                    Rectangle2D bounds = font.getStringBounds(lines[0], graphics.getFontRenderContext());

                    graphics.setColor(Color.BLACK);
                    graphics.fillRect(0, 0, image.getWidth(), image.getHeight());
                    graphics.setColor(Color.WHITE);

                    int y = (image.getHeight() + (int) bounds.getHeight()) / 4;

                    for (int i = 0; i < lines.length; i++) {
                        bounds = font.getStringBounds(lines[i], graphics.getFontRenderContext());

                        graphics.drawString(
                            lines[i],
                            image.getWidth() / 2 - (int) bounds.getWidth() / 2,
                            (y + i * (int) bounds.getHeight()) + 4
                        );
                    }

                    // Draw the border

                    if (message.getBorderType() != null) {
                        switch (message.getBorderType()) {
                            case Thin: {
                                graphics.drawRect(0, 0, 95, 31);
                                break;
                            }
                            case Thick: {
                                graphics.drawRect(0, 0, 95, 31);
                                graphics.drawRect(1, 1, 93, 29);
                                break;
                            }
                        }
                    }

                    // Send to the board

                    for (int i = 0; i < 50; i++) {
                        BLFramePacket framePacket = new BLFramePacket(image);
                        packetSender.send(framePacket.getNetworkBytes());
                        Thread.sleep(100);
                    }
                }

                catch (Exception e)
                {
                    System.out.println("Exception: " + e);
                }
            }
        }
    }
}
