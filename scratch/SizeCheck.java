public class SizeCheck {
    public static void main(String[] args) throws Exception {
        java.awt.image.BufferedImage img = javax.imageio.ImageIO.read(new java.io.File("demoproject/src/main/resources/Default/Theme/1.jpg"));
        System.out.println("1.jpg: " + img.getWidth() + "x" + img.getHeight());
    }
}
