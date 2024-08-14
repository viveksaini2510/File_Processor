

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class ImageCompressor extends JPanel {

    private BufferedImage originalImage;
    private BufferedImage compressedImage;
    private List<Cluster> clusters;
    private final int k = 5; // Number of clusters

    public ImageCompressor(String imagePath) {
        try {
            originalImage = ImageIO.read(new File(imagePath));
            compressImage();
            JFrame frame = new JFrame("Image Compressor");
            frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            frame.setSize(800, 600);
            frame.setLayout(new BorderLayout());
            frame.add(this, BorderLayout.CENTER);
            frame.setVisible(true);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void compressImage() {
        int width = originalImage.getWidth();
        int height = originalImage.getHeight();

        // Initialize clusters randomly
        clusters = initializeClusters(k);

        int[] rgbArray = originalImage.getRGB(0, 0, width, height, null, 0, width);
        List<Point> points = new ArrayList<>();

        // Convert RGB array to points
        for (int i = 0; i < rgbArray.length; i++) {
            int x = i % width;
            int y = i / width;
            points.add(new Point(x, y, rgbArray[i]));
        }

        // Run K-means clustering
        runKMeans(points);

        // Create compressed image
        compressedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        for (Point point : points) {
            int clusterColor = clusters.get(point.cluster).centroid;
            compressedImage.setRGB(point.x, point.y, clusterColor);
        }
    }

    private void runKMeans(List<Point> points) {
        for (int i = 0; i < 100; i++) { // Max iterations
            // Assign points to clusters
            for (Point point : points) {
                int minDist = Integer.MAX_VALUE;
                int closestCluster = 0;
                for (int j = 0; j < clusters.size(); j++) {
                    int dist = clusters.get(j).distance(point.rgb);
                    if (dist < minDist) {
                        minDist = dist;
                        closestCluster = j;
                    }
                }
                point.cluster = closestCluster;
            }

            // Update cluster centroids
            for (Cluster cluster : clusters) {
                int sumR = 0, sumG = 0, sumB = 0;
                int count = 0;
                for (Point point : points) {
                    if (point.cluster == clusters.indexOf(cluster)) {
                        sumR += (point.rgb >> 16) & 0xFF;
                        sumG += (point.rgb >> 8) & 0xFF;
                        sumB += point.rgb & 0xFF;
                        count++;
                    }
                }
                if (count > 0) {
                    cluster.centroid = (sumR / count << 16) | (sumG / count << 8) | (sumB / count);
                }
            }
        }
    }

    private List<Cluster> initializeClusters(int k) {
        List<Cluster> clusters = new ArrayList<>();
        Random random = new Random();
        for (int i = 0; i < k; i++) {
            int randomX = random.nextInt(originalImage.getWidth());
            int randomY = random.nextInt(originalImage.getHeight());
            int color = originalImage.getRGB(randomX, randomY);
            clusters.add(new Cluster(color));
        }
        return clusters;
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (originalImage != null && compressedImage != null) {
            g.drawImage(originalImage, 0, 0, getWidth() / 2, getHeight(), this);
            g.drawImage(compressedImage, getWidth() / 2, 0, getWidth() / 2, getHeight(), this);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ImageCompressor("C:\\Users\\anshi\\Desktop\\WhatsApp Image 2024-07-16 at 22.08.48_4e84e565.jpg"); // Replace with your image path
        });
    }

    private static class Point {
        int x, y;
        int rgb;
        int cluster;

        public Point(int x, int y, int rgb) {
            this.x = x;
            this.y = y;
            this.rgb = rgb;
            this.cluster = 0;
        }
    }

    private static class Cluster {
        int centroid;

        public Cluster(int centroid) {
            this.centroid = centroid;
        }

        public int distance(int rgb) {
            int r1 = (centroid >> 16) & 0xFF;
            int g1 = (centroid >> 8) & 0xFF;
            int b1 = centroid & 0xFF;

            int r2 = (rgb >> 16) & 0xFF;
            int g2 = (rgb >> 8) & 0xFF;
            int b2 = rgb & 0xFF;

            return (r1 - r2) * (r1 - r2) + (g1 - g2) * (g1 - g2) + (b1 - b2) * (b1 - b2);
        }
    }
}
