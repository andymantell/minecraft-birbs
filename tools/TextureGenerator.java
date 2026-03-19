import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

/**
 * Generates 64x64 bird textures with accurate colours, gradient shading,
 * and feather detail patterns from ornithological research.
 *
 * Minecraft cuboid UV mapping: for a box at texOffs(u,v) with dimensions (w,h,d):
 *   Top face:    (u+d,     v,       w, d)
 *   Bottom face: (u+d+w,   v,       w, d)
 *   Front face:  (u+d,     v+d,     w, h)
 *   Back face:   (u+d+w+d, v+d,     w, h)
 *   Left face:   (u,       v+d,     d, h)
 *   Right face:  (u+d+w,   v+d,     d, h)
 *
 * Updated for new higher-fidelity models with more cuboids per bird.
 */
public class TextureGenerator {

    static final int SIZE = 64;

    public static void main(String[] args) throws Exception {
        String basePath = "src/main/resources/assets/britishbirds/textures/entity";

        generateRobin(basePath + "/robin/robin.png");
        generateRobinJuvenile(basePath + "/robin/robin_baby.png");
        generateBlueTit(basePath + "/blue_tit/blue_tit.png");
        generateBarnOwlMale(basePath + "/barn_owl/barn_owl_male.png");
        generateBarnOwlFemale(basePath + "/barn_owl/barn_owl_female.png");
        generatePeregrineAdult(basePath + "/peregrine_falcon/peregrine_adult.png");
        generatePeregrineJuvenile(basePath + "/peregrine_falcon/peregrine_juvenile.png");
        generateMallardMale(basePath + "/mallard/mallard_male.png");
        generateMallardFemale(basePath + "/mallard/mallard_female.png");
        generateMallardDuckling(basePath + "/mallard/mallard_duckling.png");

        System.out.println("All textures generated!");
    }

    // === ROBIN ===
    // Body:      texOffs(0,0),   5x5x5
    // Breast:    texOffs(0,10),  4x3x2
    // Head:      texOffs(0,15),  4x4x4
    // Beak:      texOffs(16,15), 1x1x2
    // Crown:     texOffs(22,15), 3x1x3
    // L Wing:    texOffs(20,0),  1x4x5
    // L WingTip: texOffs(32,0),  1x3x3
    // Tail:      texOffs(0,23),  3x1x3
    // TailTip:   texOffs(12,23), 2x1x2
    // Legs:      texOffs(0,27),  1x4x1
    // Feet:      texOffs(4,27),  2x1x2

    static void generateRobin(String path) throws Exception {
        BufferedImage img = createImage();
        Graphics2D g = img.createGraphics();
        clearImage(g);

        Color oliveBrown = new Color(0x6B, 0x6B, 0x3A);
        Color oliveDark = new Color(0x5A, 0x5A, 0x2E);
        Color oliveLight = new Color(0x7B, 0x7B, 0x48);
        Color orangeRed = new Color(0xD4, 0x60, 0x2A);
        Color orangeDeep = new Color(0xC0, 0x50, 0x20);
        Color offWhite = new Color(0xF0, 0xED, 0xE0);
        Color darkBrown = new Color(0x4A, 0x3B, 0x2A);
        Color warmBuff = new Color(0xC4, 0xA8, 0x82);
        Color blueGrey = new Color(0x8B, 0x9D, 0xAF);
        Color pinkBrown = new Color(0xC4, 0xA8, 0x82);
        Color black = new Color(0x33, 0x22, 0x11);
        Color eye = new Color(0x11, 0x11, 0x11);

        // Body: texOffs(0,0), w=5, h=5, d=5
        fillBox(g, 0, 0, 5, 5, 5, oliveBrown, offWhite, orangeRed, oliveBrown, oliveBrown, oliveBrown);
        // Gradient shading on top: lighter center, darker edges
        addGradientHorizontal(img, 5, 0, 5, 5, oliveLight, oliveDark);
        // Side gradient: olive top to buff/grey border to orange
        for (int y = 5; y < 10; y++) {
            Color c = y < 7 ? oliveBrown : (y < 8 ? blueGrey : warmBuff);
            for (int x = 0; x < 5; x++) img.setRGB(x, y, c.getRGB());
            for (int x = 10; x < 15; x++) img.setRGB(x, y, c.getRGB());
        }

        // Breast: texOffs(0,10), w=4, h=3, d=2
        fillBox(g, 0, 10, 4, 3, 2, orangeRed, offWhite, orangeDeep, orangeRed, orangeRed, orangeRed);
        // Gradient on breast front: deeper orange at center
        addGradientVertical(img, 2, 12, 4, 3, orangeRed, orangeDeep);

        // Head: texOffs(0,15), w=4, h=4, d=4
        fillBox(g, 0, 15, 4, 4, 4, oliveBrown, orangeRed, orangeRed, oliveBrown, oliveBrown, oliveBrown);
        // Eyes on side faces
        img.setRGB(1, 20, eye.getRGB());
        img.setRGB(2, 20, eye.getRGB());
        img.setRGB(9, 20, eye.getRGB());
        img.setRGB(10, 20, eye.getRGB());
        // Orange cheeks blending into sides
        img.setRGB(0, 21, orangeRed.getRGB());
        img.setRGB(1, 21, orangeRed.getRGB());
        img.setRGB(11, 21, orangeRed.getRGB());
        img.setRGB(10, 21, orangeRed.getRGB());

        // Beak: texOffs(16,15), 1x1x2 - dark fine pointed
        fillBox(g, 16, 15, 1, 1, 2, black, black, black, black, black, black);

        // Crown: texOffs(22,15), 3x1x3
        fillBox(g, 22, 15, 3, 1, 3, oliveLight, oliveBrown, oliveBrown, oliveBrown, oliveBrown, oliveBrown);

        // Wings: texOffs(20,0), w=1, h=4, d=5 — dark brown with olive fringes
        fillBox(g, 20, 0, 1, 4, 5, darkBrown, darkBrown, darkBrown, darkBrown, darkBrown, darkBrown);
        // Olive fringes on top edge
        for (int x = 25; x < 26; x++) {
            img.setRGB(x, 0, oliveBrown.getRGB());
            img.setRGB(x, 1, oliveBrown.getRGB());
            img.setRGB(x, 2, oliveLight.getRGB());
        }
        // Wing bar detail
        for (int x = 25; x < 26; x++) {
            img.setRGB(x, 6, warmBuff.getRGB());
        }

        // Wing tip: texOffs(32,0), 1x3x3
        fillBox(g, 32, 0, 1, 3, 3, darkBrown, darkBrown, darkBrown, darkBrown, darkBrown, darkBrown);

        // Tail: texOffs(0,23), w=3, h=1, d=3
        fillBox(g, 0, 23, 3, 1, 3, darkBrown, darkBrown, darkBrown, darkBrown, darkBrown, darkBrown);
        // Lighter tail edges
        img.setRGB(3, 23, oliveBrown.getRGB());
        img.setRGB(5, 23, oliveBrown.getRGB());

        // Tail tip: texOffs(12,23), 2x1x2
        fillBox(g, 12, 23, 2, 1, 2, darkBrown, darkBrown, darkBrown, darkBrown, darkBrown, darkBrown);

        // Legs: texOffs(0,27), w=1, h=4, d=1
        fillBox(g, 0, 27, 1, 4, 1, pinkBrown, pinkBrown, pinkBrown, pinkBrown, pinkBrown, pinkBrown);

        // Feet: texOffs(4,27), w=2, h=1, d=2
        fillBox(g, 4, 27, 2, 1, 2, pinkBrown, pinkBrown, pinkBrown, pinkBrown, pinkBrown, pinkBrown);

        g.dispose();
        save(img, path);
        System.out.println("Generated: " + path);
    }

    static void generateRobinJuvenile(String path) throws Exception {
        BufferedImage img = createImage();
        Graphics2D g = img.createGraphics();
        clearImage(g);

        Color speckledBrown = new Color(0x8B, 0x73, 0x55);
        Color darkSpeckle = new Color(0x6B, 0x5B, 0x3A);
        Color goldenBuff = new Color(0xA8, 0x90, 0x70);
        Color pinkBrown = new Color(0xC4, 0xA8, 0x82);
        Color black = new Color(0x33, 0x22, 0x11);
        Color eye = new Color(0x11, 0x11, 0x11);

        // Body: speckled brown (no orange breast in juvenile)
        fillBox(g, 0, 0, 5, 5, 5, speckledBrown, goldenBuff, speckledBrown, speckledBrown, speckledBrown, speckledBrown);
        addSpeckles(img, 0, 0, 20, 10, darkSpeckle, goldenBuff);

        // Breast: speckled
        fillBox(g, 0, 10, 4, 3, 2, speckledBrown, goldenBuff, speckledBrown, speckledBrown, speckledBrown, speckledBrown);
        addSpeckles(img, 0, 10, 12, 5, darkSpeckle, goldenBuff);

        // Head: speckled
        fillBox(g, 0, 15, 4, 4, 4, speckledBrown, speckledBrown, speckledBrown, speckledBrown, speckledBrown, speckledBrown);
        addSpeckles(img, 0, 15, 16, 8, darkSpeckle, goldenBuff);
        img.setRGB(1, 20, eye.getRGB());
        img.setRGB(2, 20, eye.getRGB());
        img.setRGB(9, 20, eye.getRGB());
        img.setRGB(10, 20, eye.getRGB());

        // Beak
        fillBox(g, 16, 15, 1, 1, 2, black, black, black, black, black, black);

        // Crown
        fillBox(g, 22, 15, 3, 1, 3, speckledBrown, speckledBrown, speckledBrown, speckledBrown, speckledBrown, speckledBrown);

        // Wings (1x4x5 + 1x3x3)
        fillBox(g, 20, 0, 1, 4, 5, darkSpeckle, darkSpeckle, darkSpeckle, darkSpeckle, darkSpeckle, darkSpeckle);
        fillBox(g, 32, 0, 1, 3, 3, darkSpeckle, darkSpeckle, darkSpeckle, darkSpeckle, darkSpeckle, darkSpeckle);

        // Tail
        fillBox(g, 0, 23, 3, 1, 3, darkSpeckle, darkSpeckle, darkSpeckle, darkSpeckle, darkSpeckle, darkSpeckle);
        fillBox(g, 12, 23, 2, 1, 2, darkSpeckle, darkSpeckle, darkSpeckle, darkSpeckle, darkSpeckle, darkSpeckle);

        // Legs/feet
        fillBox(g, 0, 27, 1, 4, 1, pinkBrown, pinkBrown, pinkBrown, pinkBrown, pinkBrown, pinkBrown);
        fillBox(g, 4, 27, 2, 1, 2, pinkBrown, pinkBrown, pinkBrown, pinkBrown, pinkBrown, pinkBrown);

        g.dispose();
        save(img, path);
        System.out.println("Generated: " + path);
    }

    // === BLUE TIT ===
    // Body:       texOffs(0,0),   4x4x4
    // Belly:      texOffs(0,8),   3x2x3
    // Head:       texOffs(0,13),  4x4x4
    // Crown:      texOffs(16,13), 3x1x3
    // Beak:       texOffs(16,17), 1x1x1
    // Cheeks:     texOffs(20,17), 1x2x2
    // L Wing:     texOffs(16,0),  1x4x4
    // L WingTip:  texOffs(26,0),  1x3x2
    // Tail:       texOffs(0,21),  3x1x3
    // Legs:       texOffs(0,25),  1x2x1
    // Feet:       texOffs(4,25),  1x1x1

    static void generateBlueTit(String path) throws Exception {
        BufferedImage img = createImage();
        Graphics2D g = img.createGraphics();
        clearImage(g);

        Color blueCap = new Color(0x2A, 0x7A, 0xC4);          // bright cobalt blue cap
        Color blueCapLight = new Color(0x3A, 0x8A, 0xD4);
        Color yellowGreen = new Color(0x8B, 0xB0, 0x30);       // back - olive-green
        Color brightYellow = new Color(0xFF, 0xE0, 0x20);       // breast - bright yellow
        Color yellowDeep = new Color(0xF0, 0xD0, 0x10);
        Color white = new Color(0xF5, 0xF5, 0xF0);             // cheeks
        Color darkStripe = new Color(0x1A, 0x2A, 0x44);         // dark eye stripe / chin
        Color blueWing = new Color(0x4A, 0x8A, 0xC0);           // wing blue
        Color wingBar = new Color(0xE0, 0xE5, 0xF0);            // white wing bar
        Color greyBlue = new Color(0x7A, 0x8A, 0x9A);           // tail
        Color darkLeg = new Color(0x5A, 0x6A, 0x7A);            // blue-grey legs
        Color black = new Color(0x22, 0x22, 0x33);
        Color eye = new Color(0x11, 0x11, 0x11);

        // Body: texOffs(0,0), w=4, h=4, d=4
        fillBox(g, 0, 0, 4, 4, 4, yellowGreen, brightYellow, brightYellow, yellowGreen, yellowGreen, yellowGreen);
        // Dark central belly stripe
        for (int y = 4; y < 8; y++) {
            img.setRGB(5, y, darkStripe.getRGB());
            img.setRGB(6, y, darkStripe.getRGB());
        }
        // Gradient on back top
        addGradientHorizontal(img, 4, 0, 4, 4, yellowGreen, new Color(0x7B, 0xA0, 0x28));

        // Belly: texOffs(0,8), w=3, h=2, d=3
        fillBox(g, 0, 8, 3, 2, 3, brightYellow, yellowDeep, brightYellow, brightYellow, brightYellow, brightYellow);
        // Dark centre stripe continues
        img.setRGB(4, 11, darkStripe.getRGB());
        img.setRGB(5, 11, darkStripe.getRGB());

        // Head: texOffs(0,13), w=4, h=4, d=4
        fillBox(g, 0, 13, 4, 4, 4, blueCap, white, white, yellowGreen, blueCap, blueCap);
        // White cheeks on front face
        fillFace(g, 4, 17, 4, 4, white);
        // Dark eye stripe through center of front
        for (int x = 4; x < 8; x++) {
            img.setRGB(x, 18, darkStripe.getRGB());
        }
        // Dark chin strap
        img.setRGB(5, 20, darkStripe.getRGB());
        img.setRGB(6, 20, darkStripe.getRGB());
        // Eyes on side faces
        img.setRGB(1, 18, eye.getRGB());
        img.setRGB(2, 18, eye.getRGB());
        img.setRGB(9, 18, eye.getRGB());
        img.setRGB(10, 18, eye.getRGB());
        // Blue on sides of head above eye stripe
        for (int x = 0; x < 4; x++) {
            img.setRGB(x, 17, blueCap.getRGB());
        }
        for (int x = 8; x < 12; x++) {
            img.setRGB(x, 17, blueCap.getRGB());
        }
        // White below eye stripe on sides
        for (int y = 19; y < 21; y++) {
            img.setRGB(0, y, white.getRGB());
            img.setRGB(1, y, white.getRGB());
            img.setRGB(10, y, white.getRGB());
            img.setRGB(11, y, white.getRGB());
        }

        // Crown: texOffs(16,13), 3x1x3 — bright blue cap
        fillBox(g, 16, 13, 3, 1, 3, blueCapLight, blueCap, blueCap, blueCap, blueCap, blueCap);

        // Beak: texOffs(16,17), 1x1x1 — tiny dark
        fillBox(g, 16, 17, 1, 1, 1, black, black, black, black, black, black);

        // Cheeks: texOffs(20,17), 1x2x2 — white bulge
        fillBox(g, 20, 17, 1, 2, 2, white, white, white, white, white, white);

        // Wings: texOffs(16,0), w=1, h=4, d=4 — blue with white wing bar
        fillBox(g, 16, 0, 1, 4, 4, blueWing, blueWing, blueWing, blueWing, blueWing, blueWing);
        // White wing bar
        for (int x = 20; x < 21; x++) {
            img.setRGB(x, 4, wingBar.getRGB());
            img.setRGB(x, 5, wingBar.getRGB());
        }

        // Wing tip: texOffs(26,0), 1x3x2
        fillBox(g, 26, 0, 1, 3, 2, blueWing, blueWing, blueWing, blueWing, blueWing, blueWing);

        // Tail: texOffs(0,21), w=3, h=1, d=3 — grey-blue
        fillBox(g, 0, 21, 3, 1, 3, greyBlue, greyBlue, greyBlue, greyBlue, greyBlue, greyBlue);
        // Lighter edges
        img.setRGB(3, 21, new Color(0x9A, 0xAA, 0xBA).getRGB());
        img.setRGB(5, 21, new Color(0x9A, 0xAA, 0xBA).getRGB());

        // Legs: texOffs(0,25), w=1, h=2, d=1
        fillBox(g, 0, 25, 1, 2, 1, darkLeg, darkLeg, darkLeg, darkLeg, darkLeg, darkLeg);

        // Feet: texOffs(4,25), w=1, h=1, d=1
        fillBox(g, 4, 25, 1, 1, 1, darkLeg, darkLeg, darkLeg, darkLeg, darkLeg, darkLeg);

        g.dispose();
        save(img, path);
        System.out.println("Generated: " + path);
    }

    // === BARN OWL ===
    // Body:       texOffs(0,0),   5x5x5
    // LowerBody:  texOffs(0,10),  4x3x4
    // Head:       texOffs(0,17),  5x5x5
    // FacialDisc: texOffs(20,17), 6x5x1
    // Beak:       texOffs(20,23), 1x1x1
    // L Wing:     texOffs(0,27),  1x8x8
    // L WingOut:  texOffs(18,27), 1x7x5
    // Tail:       texOffs(20,0),  4x1x3
    // Legs:       texOffs(32,0),  1x5x1
    // Talons:     texOffs(36,0),  2x1x2

    static void generateBarnOwlMale(String path) throws Exception {
        BufferedImage img = createImage();
        Graphics2D g = img.createGraphics();
        clearImage(g);

        Color goldenBuff = new Color(0xC4, 0xA0, 0x55);
        Color goldenLight = new Color(0xD4, 0xB0, 0x65);
        Color white = new Color(0xF5, 0xF0, 0xE0);
        Color cream = new Color(0xF0, 0xE8, 0xD0);
        Color darkEye = new Color(0x11, 0x11, 0x11);
        Color paleHorn = new Color(0xD4, 0xC0, 0xA0);
        Color greyPink = new Color(0xB0, 0xA0, 0x90);
        Color buffRim = new Color(0xA0, 0x85, 0x60);
        Color tawnyOrange = new Color(0xB8, 0x8A, 0x48);
        Color darkSpot = new Color(0x4A, 0x3B, 0x2A);

        // Body: texOffs(0,0), w=5, h=5, d=5
        fillBox(g, 0, 0, 5, 5, 5, goldenBuff, white, white, goldenBuff, goldenBuff, goldenBuff);
        // Gradient on top: golden center to tawny edges
        addGradientHorizontal(img, 5, 0, 5, 5, goldenLight, tawnyOrange);
        // Add dorsal spots on top
        addSpeckles(img, 5, 0, 5, 5, darkSpot, goldenLight);
        // Sides: golden upper, white lower
        for (int y = 5; y < 10; y++) {
            Color c = y < 8 ? goldenBuff : white;
            for (int x = 0; x < 5; x++) img.setRGB(x, y, c.getRGB());
            for (int x = 10; x < 15; x++) img.setRGB(x, y, c.getRGB());
        }

        // Lower body: texOffs(0,10), w=4, h=3, d=4
        fillBox(g, 0, 10, 4, 3, 4, goldenBuff, white, white, goldenBuff, goldenBuff, goldenBuff);

        // Head: texOffs(0,17), w=5, h=5, d=5
        fillBox(g, 0, 17, 5, 5, 5, goldenBuff, cream, cream, goldenBuff, goldenBuff, goldenBuff);

        // Facial disc: texOffs(20,17), w=6, h=5, d=1
        // UV layout: top face (21,17,6,1), front face (21,18,6,5)
        // left face (20,18,1,5), right face (27,18,1,5)
        fillBox(g, 20, 17, 6, 5, 1, cream, cream, cream, cream, cream, cream);
        // Heart-shaped buff rim on front face (21,18)-(26,22)
        // Top rim — curved heart top
        img.setRGB(21, 18, buffRim.getRGB());
        img.setRGB(22, 18, buffRim.getRGB());
        img.setRGB(23, 18, cream.getRGB());
        img.setRGB(24, 18, cream.getRGB());
        img.setRGB(25, 18, buffRim.getRGB());
        img.setRGB(26, 18, buffRim.getRGB());
        // Side rims
        img.setRGB(21, 19, buffRim.getRGB());
        img.setRGB(26, 19, buffRim.getRGB());
        img.setRGB(21, 20, buffRim.getRGB());
        img.setRGB(26, 20, buffRim.getRGB());
        img.setRGB(21, 21, buffRim.getRGB());
        img.setRGB(26, 21, buffRim.getRGB());
        // V-shape at bottom of heart (chin)
        img.setRGB(21, 22, buffRim.getRGB());
        img.setRGB(22, 22, buffRim.getRGB());
        img.setRGB(23, 22, buffRim.getRGB());
        img.setRGB(24, 22, buffRim.getRGB());
        img.setRGB(25, 22, buffRim.getRGB());
        img.setRGB(26, 22, buffRim.getRGB());
        // Two separate dark eye spots on front face (row 2, y=20)
        img.setRGB(22, 20, darkEye.getRGB()); // left eye
        img.setRGB(25, 20, darkEye.getRGB()); // right eye
        // Subtle dark around eyes
        img.setRGB(22, 19, new Color(0x8A, 0x7A, 0x6A).getRGB());
        img.setRGB(25, 19, new Color(0x8A, 0x7A, 0x6A).getRGB());
        // Left/right side faces
        img.setRGB(20, 18, buffRim.getRGB());
        img.setRGB(27, 18, buffRim.getRGB());

        // Beak: texOffs(20,23), 1x1x1
        fillBox(g, 20, 23, 1, 1, 1, paleHorn, paleHorn, paleHorn, paleHorn, paleHorn, paleHorn);

        // Wings: texOffs(0,27), w=1, h=8, d=8 — huge broad rounded owl wings
        fillBox(g, 0, 27, 1, 8, 8, goldenBuff, goldenBuff, goldenBuff, goldenBuff, goldenBuff, goldenBuff);
        // Add barring on wings
        for (int y = 35; y < 43; y += 2) {
            for (int x = 1; x < 9; x++) {
                if (x < SIZE && y < SIZE) img.setRGB(x, y, tawnyOrange.getRGB());
            }
        }
        // Lighter feather edges
        for (int y = 35; y < 43; y++) {
            if (y % 3 == 0) {
                for (int x = 1; x < 9; x++) {
                    if (x < SIZE && y < SIZE) img.setRGB(x, y, goldenLight.getRGB());
                }
            }
        }

        // Wing outer: texOffs(18,27), w=1, h=7, d=5
        fillBox(g, 18, 27, 1, 7, 5, goldenBuff, goldenBuff, goldenBuff, goldenBuff, goldenBuff, goldenBuff);

        // Tail: texOffs(20,0), w=4, h=1, d=3
        fillBox(g, 20, 0, 4, 1, 3, goldenBuff, goldenBuff, goldenBuff, goldenBuff, goldenBuff, goldenBuff);
        // Dark bars
        for (int x = 23; x < 27; x++) {
            img.setRGB(x, 3, tawnyOrange.getRGB());
        }

        // Legs: texOffs(32,0), w=1, h=5, d=1
        fillBox(g, 32, 0, 1, 5, 1, greyPink, greyPink, greyPink, greyPink, white, greyPink);

        // Talons: texOffs(36,0), w=2, h=1, d=2
        fillBox(g, 36, 0, 2, 1, 2, greyPink, greyPink, greyPink, greyPink, greyPink, greyPink);

        g.dispose();
        save(img, path);
        System.out.println("Generated: " + path);
    }

    static void generateBarnOwlFemale(String path) throws Exception {
        BufferedImage img = createImage();
        Graphics2D g = img.createGraphics();
        clearImage(g);

        Color goldenBuff = new Color(0xC4, 0xA0, 0x55);
        Color goldenLight = new Color(0xD4, 0xB0, 0x65);
        Color buffWhite = new Color(0xE8, 0xDD, 0xC8);
        Color cream = new Color(0xF0, 0xE8, 0xD0);
        Color darkEye = new Color(0x11, 0x11, 0x11);
        Color paleHorn = new Color(0xD4, 0xC0, 0xA0);
        Color greyPink = new Color(0xB0, 0xA0, 0x90);
        Color buffRim = new Color(0xA0, 0x85, 0x60);
        Color tawnyOrange = new Color(0xB8, 0x8A, 0x48);
        Color darkSpot = new Color(0x4A, 0x3B, 0x2A);

        // Body: same as male but buffier breast with spots
        fillBox(g, 0, 0, 5, 5, 5, goldenBuff, buffWhite, buffWhite, goldenBuff, goldenBuff, goldenBuff);
        addGradientHorizontal(img, 5, 0, 5, 5, goldenLight, tawnyOrange);
        addSpeckles(img, 5, 0, 5, 5, darkSpot, goldenLight);
        // Spots on breast (female characteristic)
        addSpeckles(img, 5, 5, 5, 5, darkSpot, buffWhite);
        for (int y = 5; y < 10; y++) {
            Color c = y < 8 ? goldenBuff : buffWhite;
            for (int x = 0; x < 5; x++) img.setRGB(x, y, c.getRGB());
            for (int x = 10; x < 15; x++) img.setRGB(x, y, c.getRGB());
        }

        // Lower body
        fillBox(g, 0, 10, 4, 3, 4, goldenBuff, buffWhite, buffWhite, goldenBuff, goldenBuff, goldenBuff);
        addSpeckles(img, 4, 14, 4, 3, darkSpot, buffWhite);

        // Head
        fillBox(g, 0, 17, 5, 5, 5, goldenBuff, cream, cream, goldenBuff, goldenBuff, goldenBuff);

        // Facial disc — same heart-shape as male
        fillBox(g, 20, 17, 6, 5, 1, cream, cream, cream, cream, cream, cream);
        // Heart-shaped buff rim on front face (21,18)-(26,22)
        img.setRGB(21, 18, buffRim.getRGB());
        img.setRGB(22, 18, buffRim.getRGB());
        img.setRGB(25, 18, buffRim.getRGB());
        img.setRGB(26, 18, buffRim.getRGB());
        img.setRGB(21, 19, buffRim.getRGB());
        img.setRGB(26, 19, buffRim.getRGB());
        img.setRGB(21, 20, buffRim.getRGB());
        img.setRGB(26, 20, buffRim.getRGB());
        img.setRGB(21, 21, buffRim.getRGB());
        img.setRGB(26, 21, buffRim.getRGB());
        for (int x = 21; x <= 26; x++) img.setRGB(x, 22, buffRim.getRGB());
        // Two separate dark eye spots
        img.setRGB(22, 20, darkEye.getRGB());
        img.setRGB(25, 20, darkEye.getRGB());
        img.setRGB(22, 19, new Color(0x8A, 0x7A, 0x6A).getRGB());
        img.setRGB(25, 19, new Color(0x8A, 0x7A, 0x6A).getRGB());
        img.setRGB(20, 18, buffRim.getRGB());
        img.setRGB(27, 18, buffRim.getRGB());

        // Beak
        fillBox(g, 20, 23, 1, 1, 1, paleHorn, paleHorn, paleHorn, paleHorn, paleHorn, paleHorn);

        // Wings (1x8x8 + 1x7x5 — same huge proportions as male)
        fillBox(g, 0, 27, 1, 8, 8, goldenBuff, goldenBuff, goldenBuff, goldenBuff, goldenBuff, goldenBuff);
        for (int y = 35; y < 43; y += 2) {
            for (int x = 1; x < 9; x++) {
                if (x < SIZE && y < SIZE) img.setRGB(x, y, tawnyOrange.getRGB());
            }
        }
        fillBox(g, 18, 27, 1, 7, 5, goldenBuff, goldenBuff, goldenBuff, goldenBuff, goldenBuff, goldenBuff);

        // Tail
        fillBox(g, 20, 0, 4, 1, 3, goldenBuff, goldenBuff, goldenBuff, goldenBuff, goldenBuff, goldenBuff);

        // Legs/talons
        fillBox(g, 32, 0, 1, 5, 1, greyPink, greyPink, greyPink, greyPink, buffWhite, greyPink);
        fillBox(g, 36, 0, 2, 1, 2, greyPink, greyPink, greyPink, greyPink, greyPink, greyPink);

        g.dispose();
        save(img, path);
        System.out.println("Generated: " + path);
    }

    // === PEREGRINE FALCON ===
    // Chest:       texOffs(0,0),   6x5x5
    // RearBody:    texOffs(0,10),  4x4x5
    // Head:        texOffs(0,19),  3x3x3
    // Beak:        texOffs(12,19), 1x1x2
    // BeakHook:    texOffs(18,19), 1x1x1
    // Malar:       texOffs(22,19), 1x2x2
    // L Wing:      texOffs(0,25),  1x6x7
    // L WingOuter: texOffs(16,25), 1x5x5
    // Tail:        texOffs(22,0),  3x1x4
    // TailTip:     texOffs(22,5),  2x1x3
    // Legs:        texOffs(36,0),  1x5x1
    // Talons:      texOffs(40,0),  2x1x2

    static void generatePeregrineAdult(String path) throws Exception {
        BufferedImage img = createImage();
        Graphics2D g = img.createGraphics();
        clearImage(g);

        Color slateGrey = new Color(0x4A, 0x55, 0x68);
        Color darkSlate = new Color(0x3A, 0x42, 0x52);
        Color slateLight = new Color(0x5A, 0x65, 0x78);
        Color paleBarred = new Color(0xE8, 0xE0, 0xD5);
        Color darkBarring = new Color(0x55, 0x4A, 0x3E);
        Color blackHead = new Color(0x1A, 0x1A, 0x22);
        Color whiteCheek = new Color(0xF0, 0xEC, 0xE5);
        Color yellowCere = new Color(0xDA, 0xA5, 0x20);
        Color yellowFeet = new Color(0xDA, 0xA5, 0x20);
        Color darkBeak = new Color(0x2A, 0x2A, 0x33);
        Color darkEye = new Color(0x11, 0x11, 0x11);

        // Chest: texOffs(0,0), w=6, h=5, d=5 — slate back, pale barred front
        fillBox(g, 0, 0, 6, 5, 5, slateGrey, paleBarred, paleBarred, slateGrey, slateGrey, slateGrey);
        // Back gradient: darker center slate
        addGradientHorizontal(img, 5, 0, 6, 5, slateLight, darkSlate);
        // Front barring pattern
        for (int y = 5; y < 10; y++) {
            Color c = (y % 2 == 0) ? paleBarred : darkBarring;
            for (int x = 5; x < 11; x++) img.setRGB(x, y, c.getRGB());
        }
        // Sides: slate upper, barred lower
        for (int y = 5; y < 10; y++) {
            Color c = y < 7 ? slateGrey : ((y % 2 == 0) ? paleBarred : darkBarring);
            for (int x = 0; x < 5; x++) img.setRGB(x, y, c.getRGB());
            for (int x = 11; x < 16; x++) img.setRGB(x, y, c.getRGB());
        }

        // Rear body: texOffs(0,10), w=4, h=4, d=5 — continues the slate/barred pattern
        fillBox(g, 0, 10, 4, 4, 5, slateGrey, paleBarred, paleBarred, slateGrey, slateGrey, slateGrey);
        // Barring on front of rear body
        for (int y = 15; y < 19; y++) {
            Color c = (y % 2 == 0) ? paleBarred : darkBarring;
            for (int x = 5; x < 9; x++) img.setRGB(x, y, c.getRGB());
        }

        // Head: texOffs(0,19), w=3, h=3, d=3 — dark helmet
        fillBox(g, 0, 19, 3, 3, 3, blackHead, blackHead, blackHead, blackHead, blackHead, blackHead);
        // Front face: dark cap, white cheek, dark moustache
        fillFace(g, 3, 22, 3, 3, blackHead);
        // White cheeks on front
        img.setRGB(3, 23, whiteCheek.getRGB());
        img.setRGB(5, 23, whiteCheek.getRGB());
        img.setRGB(4, 24, whiteCheek.getRGB());
        // White throat on bottom
        fillFace(g, 6, 19, 3, 3, whiteCheek);
        // Side faces: dark with white throat patch
        img.setRGB(1, 23, darkEye.getRGB());  // left eye
        img.setRGB(7, 23, darkEye.getRGB());  // right eye
        img.setRGB(0, 24, whiteCheek.getRGB());
        img.setRGB(8, 24, whiteCheek.getRGB());
        img.setRGB(2, 24, whiteCheek.getRGB());
        img.setRGB(6, 24, whiteCheek.getRGB());

        // Beak: texOffs(12,19), 1x1x2 — dark hooked
        fillBox(g, 12, 19, 1, 1, 2, darkBeak, darkBeak, darkBeak, darkBeak, darkBeak, darkBeak);
        // Yellow cere at base
        img.setRGB(14, 20, yellowCere.getRGB());
        img.setRGB(14, 21, yellowCere.getRGB());

        // Beak hook: texOffs(18,19), 1x1x1
        fillBox(g, 18, 19, 1, 1, 1, darkBeak, darkBeak, darkBeak, darkBeak, darkBeak, darkBeak);

        // Malar stripe: texOffs(22,19), 1x2x2
        fillBox(g, 22, 19, 1, 2, 2, blackHead, blackHead, blackHead, blackHead, blackHead, blackHead);

        // Wings: texOffs(0,25), w=1, h=6, d=7 — long narrow sickle-shaped
        fillBox(g, 0, 25, 1, 6, 7, slateGrey, slateGrey, slateGrey, slateGrey, slateGrey, slateGrey);
        // Darker wingtips
        for (int x = 6; x < 8; x++) {
            for (int y = 32; y < 38; y++) {
                if (x < SIZE && y < SIZE) img.setRGB(x, y, darkSlate.getRGB());
            }
        }
        // Subtle barring on wing underside
        for (int y = 32; y < 38; y += 2) {
            img.setRGB(1, y, slateLight.getRGB());
        }

        // Wing outer: texOffs(16,25), w=1, h=5, d=5 — pointed tip darker
        fillBox(g, 16, 25, 1, 5, 5, slateGrey, slateGrey, slateGrey, slateGrey, slateGrey, slateGrey);
        // Darker tips
        for (int x = 21; x < 22; x++) {
            for (int y = 30; y < 35; y++) {
                if (x < SIZE && y < SIZE) img.setRGB(x, y, darkSlate.getRGB());
            }
        }

        // Tail: texOffs(22,0), w=3, h=1, d=4 — slate with dark bars
        fillBox(g, 22, 0, 3, 1, 4, slateGrey, slateGrey, slateGrey, slateGrey, slateGrey, slateGrey);
        // Dark subterminal band
        for (int x = 26; x < 29; x++) {
            img.setRGB(x, 4, darkSlate.getRGB());
        }
        // White tip
        for (int x = 26; x < 29; x++) {
            img.setRGB(x, 3, slateLight.getRGB());
        }

        // Tail tip: texOffs(22,5), w=2, h=1, d=3
        fillBox(g, 22, 5, 2, 1, 3, slateGrey, slateGrey, slateGrey, slateGrey, slateGrey, slateGrey);
        // Dark band near tip
        for (int x = 25; x < 27; x++) {
            img.setRGB(x, 8, darkSlate.getRGB());
        }

        // Legs: texOffs(36,0), w=1, h=5, d=1 — yellow
        fillBox(g, 36, 0, 1, 5, 1, yellowFeet, yellowFeet, yellowFeet, yellowFeet, yellowFeet, yellowFeet);

        // Talons: texOffs(40,0), w=2, h=1, d=2 — yellow with dark claws
        fillBox(g, 40, 0, 2, 1, 2, yellowFeet, yellowFeet, yellowFeet, yellowFeet, yellowFeet, yellowFeet);
        // Dark talon tips
        img.setRGB(42, 2, darkBeak.getRGB());
        img.setRGB(43, 2, darkBeak.getRGB());

        g.dispose();
        save(img, path);
        System.out.println("Generated: " + path);
    }

    static void generatePeregrineJuvenile(String path) throws Exception {
        BufferedImage img = createImage();
        Graphics2D g = img.createGraphics();
        clearImage(g);

        Color brown = new Color(0x6B, 0x55, 0x3A);
        Color brownLight = new Color(0x7B, 0x65, 0x4A);
        Color buffCream = new Color(0xE0, 0xD0, 0xB5);
        Color darkStreak = new Color(0x4A, 0x3B, 0x2A);
        Color darkHead = new Color(0x3A, 0x2E, 0x22);
        Color buffCheek = new Color(0xD0, 0xC0, 0xA5);
        Color blueGreyFeet = new Color(0x7A, 0x85, 0x90);
        Color darkBeak = new Color(0x2A, 0x2A, 0x33);
        Color darkEye = new Color(0x11, 0x11, 0x11);

        // Chest: brown back, streaked buff front
        fillBox(g, 0, 0, 6, 5, 5, brown, buffCream, buffCream, brown, brown, brown);
        // Vertical streaks on front (not horizontal bars like adult)
        for (int x = 5; x < 11; x++) {
            if (x % 2 == 0) {
                for (int y = 5; y < 10; y++) img.setRGB(x, y, darkStreak.getRGB());
            }
        }

        // Rear body
        fillBox(g, 0, 10, 4, 4, 5, brown, buffCream, buffCream, brown, brown, brown);
        for (int x = 5; x < 9; x++) {
            if (x % 2 == 0) {
                for (int y = 15; y < 19; y++) img.setRGB(x, y, darkStreak.getRGB());
            }
        }

        // Head: brown (not black like adult)
        fillBox(g, 0, 19, 3, 3, 3, darkHead, darkHead, darkHead, darkHead, darkHead, darkHead);
        img.setRGB(3, 23, buffCheek.getRGB());
        img.setRGB(5, 23, buffCheek.getRGB());
        img.setRGB(4, 24, buffCheek.getRGB());
        img.setRGB(1, 23, darkEye.getRGB());
        img.setRGB(7, 23, darkEye.getRGB());
        img.setRGB(0, 24, buffCheek.getRGB());
        img.setRGB(8, 24, buffCheek.getRGB());

        // Beak, beak hook, malar
        fillBox(g, 12, 19, 1, 1, 2, darkBeak, darkBeak, darkBeak, darkBeak, darkBeak, darkBeak);
        fillBox(g, 18, 19, 1, 1, 1, darkBeak, darkBeak, darkBeak, darkBeak, darkBeak, darkBeak);
        fillBox(g, 22, 19, 1, 2, 2, darkHead, darkHead, darkHead, darkHead, darkHead, darkHead);

        // Wings (1x6x7 + 1x5x5)
        fillBox(g, 0, 25, 1, 6, 7, brown, brown, brown, brown, brown, brown);
        fillBox(g, 16, 25, 1, 5, 5, brown, brown, brown, brown, brown, brown);

        // Tail
        fillBox(g, 22, 0, 3, 1, 4, brown, brown, brown, brown, brown, brown);
        fillBox(g, 22, 5, 2, 1, 3, brown, brown, brown, brown, brown, brown);

        // Legs/talons (1x5x1 to match model)
        fillBox(g, 36, 0, 1, 5, 1, blueGreyFeet, blueGreyFeet, blueGreyFeet, blueGreyFeet, blueGreyFeet, blueGreyFeet);
        fillBox(g, 40, 0, 2, 1, 2, blueGreyFeet, blueGreyFeet, blueGreyFeet, blueGreyFeet, blueGreyFeet, blueGreyFeet);

        g.dispose();
        save(img, path);
        System.out.println("Generated: " + path);
    }

    // === MALLARD ===
    // Body:       texOffs(0,0),   7x6x6
    // RearBody:   texOffs(0,12),  5x5x5
    // Neck:       texOffs(20,12), 3x3x2
    // Head:       texOffs(0,22),  4x4x4
    // Crown:      texOffs(16,22), 3x1x3
    // Bill:       texOffs(16,26), 3x1x3
    // BillTip:    texOffs(28,26), 3x1x1
    // L Wing:     texOffs(0,30),  1x7x8
    // L WingTip:  texOffs(18,30), 1x4x4
    // Tail:       texOffs(26,0),  5x1x4
    // TailCurl:   texOffs(26,5),  1x1x2
    // Legs:       texOffs(26,8),  1x4x1
    // Feet:       texOffs(30,8),  3x1x3

    static void generateMallardMale(String path) throws Exception {
        BufferedImage img = createImage();
        Graphics2D g = img.createGraphics();
        clearImage(g);

        Color iridGreen = new Color(0x2D, 0x6B, 0x33);
        Color iridGreenLight = new Color(0x3D, 0x7B, 0x43);
        Color chestnut = new Color(0x8B, 0x45, 0x13);
        Color chestnutDeep = new Color(0x7B, 0x35, 0x08);
        Color greyFlanks = new Color(0xB0, 0xAA, 0x9E);
        Color greyLight = new Color(0xC0, 0xBA, 0xAE);
        Color blackRump = new Color(0x1A, 0x1A, 0x1A);
        Color whiteRing = new Color(0xF0, 0xF0, 0xF0);
        Color yellowBill = new Color(0xDA, 0xA5, 0x20);
        Color yellowBillTip = new Color(0xC0, 0x90, 0x15);
        Color orangeLegs = new Color(0xFF, 0x8C, 0x00);
        Color speculum = new Color(0x6A, 0x5A, 0xCD);
        Color speculumWhite = new Color(0xF0, 0xF0, 0xF0);
        Color greyBrown = new Color(0x8A, 0x7E, 0x72);
        Color darkEye = new Color(0x11, 0x11, 0x11);

        // Body: texOffs(0,0), w=7, h=6, d=6
        fillBox(g, 0, 0, 7, 6, 6, greyBrown, greyFlanks, chestnut, greyBrown, greyBrown, greyBrown);
        // Gradient on top (back): grey-brown with subtle vermiculation
        addGradientHorizontal(img, 6, 0, 7, 6, greyBrown, new Color(0x7A, 0x6E, 0x62));
        // Chestnut breast on front with gradient
        addGradientVertical(img, 6, 6, 7, 6, chestnut, chestnutDeep);
        // Sides: chestnut upper transitioning to grey flanks
        for (int y = 6; y < 12; y++) {
            Color c = y < 9 ? chestnut : greyFlanks;
            for (int x = 0; x < 6; x++) img.setRGB(x, y, c.getRGB());
            for (int x = 13; x < 19; x++) img.setRGB(x, y, c.getRGB());
        }
        // Black rump on back at rear
        for (int x = 6; x < 13; x++) {
            img.setRGB(x, 0, blackRump.getRGB());
            img.setRGB(x, 1, blackRump.getRGB());
        }

        // Rear body: texOffs(0,12), w=5, h=5, d=5
        fillBox(g, 0, 12, 5, 5, 5, greyBrown, greyFlanks, greyFlanks, greyBrown, greyBrown, greyBrown);
        // Black rump area
        for (int x = 5; x < 10; x++) {
            img.setRGB(x, 12, blackRump.getRGB());
            img.setRGB(x, 13, blackRump.getRGB());
        }
        // Fine vermiculation on flanks
        addSpeckles(img, 5, 17, 5, 5, new Color(0x9A, 0x8E, 0x82), greyLight);

        // Neck: texOffs(20,12), w=3, h=3, d=2 — white ring at base of green
        fillBox(g, 20, 12, 3, 3, 2, iridGreen, whiteRing, iridGreen, iridGreen, iridGreen, iridGreen);
        // White ring at bottom
        for (int x = 22; x < 25; x++) {
            img.setRGB(x, 12, whiteRing.getRGB());
            img.setRGB(x, 13, whiteRing.getRGB());
        }

        // Head: texOffs(0,22), w=4, h=4, d=4 — iridescent green
        fillBox(g, 0, 22, 4, 4, 4, iridGreen, iridGreen, iridGreen, iridGreen, iridGreen, iridGreen);
        // Gradient: lighter on top for iridescent sheen
        addGradientHorizontal(img, 4, 22, 4, 4, iridGreenLight, iridGreen);
        // Eyes
        img.setRGB(1, 28, darkEye.getRGB());
        img.setRGB(2, 28, darkEye.getRGB());
        img.setRGB(9, 28, darkEye.getRGB());
        img.setRGB(10, 28, darkEye.getRGB());

        // Crown dome: texOffs(16,22), 3x1x3
        fillBox(g, 16, 22, 3, 1, 3, iridGreen, iridGreen, iridGreen, iridGreen, iridGreen, iridGreen);

        // Bill: texOffs(16,26), w=3, h=1, d=3 — yellow
        fillBox(g, 16, 26, 3, 1, 3, yellowBill, yellowBill, yellowBill, yellowBill, yellowBill, yellowBill);
        // Black nail at tip
        img.setRGB(22, 27, new Color(0x1A, 0x1A, 0x1A).getRGB());

        // Bill tip: texOffs(28,26), 3x1x1
        fillBox(g, 28, 26, 3, 1, 1, yellowBillTip, yellowBillTip, yellowBillTip, yellowBillTip, yellowBillTip, yellowBillTip);
        // Black nail
        img.setRGB(29, 27, new Color(0x1A, 0x1A, 0x1A).getRGB());

        // Wings: texOffs(0,30), w=1, h=7, d=8
        fillBox(g, 0, 30, 1, 7, 8, greyBrown, greyBrown, greyBrown, greyBrown, greyBrown, greyBrown);

        // Wing tip (speculum area): texOffs(18,30), w=1, h=4, d=4
        fillBox(g, 18, 30, 1, 4, 4, speculum, speculum, speculum, speculum, speculum, speculum);
        // White borders on speculum
        for (int y = 34; y < 38; y++) {
            img.setRGB(19, y, speculumWhite.getRGB());
            img.setRGB(22, y, speculumWhite.getRGB());
        }

        // Tail: texOffs(26,0), w=5, h=1, d=4
        fillBox(g, 26, 0, 5, 1, 4, greyFlanks, greyFlanks, greyFlanks, greyFlanks, blackRump, greyFlanks);

        // Tail curl: texOffs(26,5), 1x1x2 — black
        fillBox(g, 26, 5, 1, 1, 2, blackRump, blackRump, blackRump, blackRump, blackRump, blackRump);

        // Legs: texOffs(26,8), w=1, h=4, d=1 — orange
        fillBox(g, 26, 8, 1, 4, 1, orangeLegs, orangeLegs, orangeLegs, orangeLegs, orangeLegs, orangeLegs);

        // Feet: texOffs(30,8), w=3, h=1, d=3 — orange webbed
        fillBox(g, 30, 8, 3, 1, 3, orangeLegs, orangeLegs, orangeLegs, orangeLegs, orangeLegs, orangeLegs);

        g.dispose();
        save(img, path);
        System.out.println("Generated: " + path);
    }

    static void generateMallardFemale(String path) throws Exception {
        BufferedImage img = createImage();
        Graphics2D g = img.createGraphics();
        clearImage(g);

        Color mottledBrown = new Color(0x8B, 0x73, 0x55);
        Color darkBrown = new Color(0x5A, 0x48, 0x30);
        Color buffEdge = new Color(0xC4, 0xAA, 0x82);
        Color darkCrown = new Color(0x4A, 0x3B, 0x2A);
        Color buffFace = new Color(0xD0, 0xBB, 0x95);
        Color darkEyeStripe = new Color(0x3A, 0x2E, 0x22);
        Color orangeBill = new Color(0xC0, 0x80, 0x40);
        Color orangeLegs = new Color(0xE0, 0x80, 0x20);
        Color speculum = new Color(0x6A, 0x5A, 0xCD);
        Color speculumWhite = new Color(0xF0, 0xF0, 0xF0);
        Color darkEye = new Color(0x11, 0x11, 0x11);

        // Body: mottled brown all over (cryptic)
        fillBox(g, 0, 0, 7, 6, 6, mottledBrown, mottledBrown, mottledBrown, mottledBrown, mottledBrown, mottledBrown);
        addSpeckles(img, 0, 0, 26, 12, darkBrown, buffEdge);

        // Rear body
        fillBox(g, 0, 12, 5, 5, 5, mottledBrown, mottledBrown, mottledBrown, mottledBrown, mottledBrown, mottledBrown);
        addSpeckles(img, 0, 12, 20, 10, darkBrown, buffEdge);

        // Neck: mottled
        fillBox(g, 20, 12, 3, 3, 2, mottledBrown, mottledBrown, mottledBrown, mottledBrown, mottledBrown, mottledBrown);

        // Head: dark crown, buff cheeks with eye stripe
        fillBox(g, 0, 22, 4, 4, 4, darkCrown, buffFace, buffFace, darkCrown, darkCrown, darkCrown);
        // Eye stripes on sides
        img.setRGB(1, 28, darkEye.getRGB());
        img.setRGB(2, 28, darkEyeStripe.getRGB());
        img.setRGB(9, 28, darkEye.getRGB());
        img.setRGB(10, 28, darkEyeStripe.getRGB());
        // Buff below eye
        img.setRGB(0, 29, buffFace.getRGB());
        img.setRGB(1, 29, buffFace.getRGB());
        img.setRGB(10, 29, buffFace.getRGB());
        img.setRGB(11, 29, buffFace.getRGB());

        // Crown
        fillBox(g, 16, 22, 3, 1, 3, darkCrown, darkCrown, darkCrown, darkCrown, darkCrown, darkCrown);

        // Bill: mottled orange-brown
        fillBox(g, 16, 26, 3, 1, 3, orangeBill, orangeBill, orangeBill, orangeBill, orangeBill, orangeBill);
        fillBox(g, 28, 26, 3, 1, 1, orangeBill, orangeBill, orangeBill, orangeBill, orangeBill, orangeBill);

        // Wings: brown with speculum (1x7x8 + 1x4x4)
        fillBox(g, 0, 30, 1, 7, 8, mottledBrown, mottledBrown, mottledBrown, mottledBrown, mottledBrown, mottledBrown);
        fillBox(g, 18, 30, 1, 4, 4, speculum, speculum, speculum, speculum, speculum, speculum);
        for (int y = 34; y < 38; y++) {
            img.setRGB(19, y, speculumWhite.getRGB());
            img.setRGB(22, y, speculumWhite.getRGB());
        }

        // Tail
        fillBox(g, 26, 0, 5, 1, 4, mottledBrown, mottledBrown, mottledBrown, mottledBrown, mottledBrown, mottledBrown);
        // No tail curl visible for female, but fill the UV space
        fillBox(g, 26, 5, 1, 1, 2, mottledBrown, mottledBrown, mottledBrown, mottledBrown, mottledBrown, mottledBrown);

        // Legs/feet (1x4x1 to match model)
        fillBox(g, 26, 8, 1, 4, 1, orangeLegs, orangeLegs, orangeLegs, orangeLegs, orangeLegs, orangeLegs);
        fillBox(g, 30, 8, 3, 1, 3, orangeLegs, orangeLegs, orangeLegs, orangeLegs, orangeLegs, orangeLegs);

        g.dispose();
        save(img, path);
        System.out.println("Generated: " + path);
    }

    static void generateMallardDuckling(String path) throws Exception {
        BufferedImage img = createImage();
        Graphics2D g = img.createGraphics();
        clearImage(g);

        Color yellow = new Color(0xFF, 0xD7, 0x00);
        Color yellowLight = new Color(0xFF, 0xE7, 0x30);
        Color darkBack = new Color(0x3A, 0x2E, 0x22);
        Color darkStripe = new Color(0x4A, 0x3B, 0x2A);
        Color greyBill = new Color(0x55, 0x55, 0x55);
        Color greyLegs = new Color(0x55, 0x55, 0x55);
        Color darkEye = new Color(0x11, 0x11, 0x11);

        // Body: dark back, yellow belly/front
        fillBox(g, 0, 0, 7, 6, 6, darkBack, yellow, yellow, darkBack, yellow, darkBack);
        // Yellow spots on dark back
        img.setRGB(8, 1, yellow.getRGB());
        img.setRGB(11, 1, yellow.getRGB());
        img.setRGB(8, 4, yellow.getRGB());
        img.setRGB(11, 4, yellow.getRGB());
        // Sides: yellow lower half
        for (int y = 6; y < 12; y++) {
            Color c = y < 9 ? darkBack : yellow;
            for (int x = 0; x < 6; x++) img.setRGB(x, y, c.getRGB());
            for (int x = 13; x < 19; x++) img.setRGB(x, y, c.getRGB());
        }

        // Rear body
        fillBox(g, 0, 12, 5, 5, 5, darkBack, yellow, yellow, darkBack, yellow, darkBack);

        // Neck
        fillBox(g, 20, 12, 3, 3, 2, darkBack, yellow, yellow, darkBack, yellow, darkBack);

        // Head: yellow face, dark crown
        fillBox(g, 0, 22, 4, 4, 4, darkBack, yellow, yellow, darkBack, yellow, darkBack);
        img.setRGB(1, 28, darkEye.getRGB());
        img.setRGB(2, 28, darkEye.getRGB());
        img.setRGB(9, 28, darkEye.getRGB());
        img.setRGB(10, 28, darkEye.getRGB());
        img.setRGB(0, 28, darkStripe.getRGB());
        img.setRGB(11, 28, darkStripe.getRGB());

        // Crown
        fillBox(g, 16, 22, 3, 1, 3, darkBack, darkBack, darkBack, darkBack, darkBack, darkBack);

        // Bill: grey
        fillBox(g, 16, 26, 3, 1, 3, greyBill, greyBill, greyBill, greyBill, greyBill, greyBill);
        fillBox(g, 28, 26, 3, 1, 1, greyBill, greyBill, greyBill, greyBill, greyBill, greyBill);

        // Wings: dark (1x7x8 + 1x4x4)
        fillBox(g, 0, 30, 1, 7, 8, darkBack, darkBack, darkBack, darkBack, darkBack, darkBack);
        fillBox(g, 18, 30, 1, 4, 4, darkBack, darkBack, darkBack, darkBack, darkBack, darkBack);

        // Tail
        fillBox(g, 26, 0, 5, 1, 4, darkBack, darkBack, darkBack, darkBack, darkBack, darkBack);
        fillBox(g, 26, 5, 1, 1, 2, darkBack, darkBack, darkBack, darkBack, darkBack, darkBack);

        // Legs/feet: dark grey (1x4x1 to match model)
        fillBox(g, 26, 8, 1, 4, 1, greyLegs, greyLegs, greyLegs, greyLegs, greyLegs, greyLegs);
        fillBox(g, 30, 8, 3, 1, 3, greyLegs, greyLegs, greyLegs, greyLegs, greyLegs, greyLegs);

        g.dispose();
        save(img, path);
        System.out.println("Generated: " + path);
    }

    // === HELPER METHODS ===

    static BufferedImage createImage() {
        return new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
    }

    static void clearImage(Graphics2D g) {
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(0, 0, SIZE, SIZE);
        g.setComposite(AlphaComposite.SrcOver);
    }

    /**
     * Fill a cuboid UV region. For a box at texOffs(u,v) with dims (w,h,d):
     * Top:    (u+d,   v,     w, d)
     * Bottom: (u+d+w, v,     w, d)
     * Left:   (u,     v+d,   d, h)
     * Front:  (u+d,   v+d,   w, h)
     * Right:  (u+d+w, v+d,   d, h)
     * Back:   (u+2d+w,v+d,   w, h)
     */
    static void fillBox(Graphics2D g, int u, int v, int w, int h, int d,
                         Color top, Color bottom, Color front, Color back, Color left, Color right) {
        g.setColor(top);
        g.fillRect(u + d, v, w, d);
        g.setColor(bottom);
        g.fillRect(u + d + w, v, w, d);
        g.setColor(left);
        g.fillRect(u, v + d, d, h);
        g.setColor(front);
        g.fillRect(u + d, v + d, w, h);
        g.setColor(right);
        g.fillRect(u + d + w, v + d, d, h);
        g.setColor(back);
        g.fillRect(u + 2 * d + w, v + d, w, h);
    }

    static void fillFace(Graphics2D g, int x, int y, int w, int h, Color c) {
        g.setColor(c);
        g.fillRect(x, y, w, h);
    }

    static void addSpeckles(BufferedImage img, int startX, int startY, int w, int h,
                            Color dark, Color light) {
        for (int y = startY; y < startY + h && y < SIZE; y++) {
            for (int x = startX; x < startX + w && x < SIZE; x++) {
                if ((x + y) % 3 == 0) {
                    img.setRGB(x, y, dark.getRGB());
                } else if ((x + y) % 5 == 0) {
                    img.setRGB(x, y, light.getRGB());
                }
            }
        }
    }

    /**
     * Add a horizontal gradient across a region (left to right transition).
     */
    static void addGradientHorizontal(BufferedImage img, int startX, int startY, int w, int h,
                                       Color from, Color to) {
        for (int x = startX; x < startX + w && x < SIZE; x++) {
            float ratio = (float)(x - startX) / Math.max(1, w - 1);
            int r = (int)(from.getRed() + ratio * (to.getRed() - from.getRed()));
            int gr = (int)(from.getGreen() + ratio * (to.getGreen() - from.getGreen()));
            int b = (int)(from.getBlue() + ratio * (to.getBlue() - from.getBlue()));
            Color c = new Color(clamp(r), clamp(gr), clamp(b));
            for (int y = startY; y < startY + h && y < SIZE; y++) {
                img.setRGB(x, y, c.getRGB());
            }
        }
    }

    /**
     * Add a vertical gradient across a region (top to bottom transition).
     */
    static void addGradientVertical(BufferedImage img, int startX, int startY, int w, int h,
                                     Color from, Color to) {
        for (int y = startY; y < startY + h && y < SIZE; y++) {
            float ratio = (float)(y - startY) / Math.max(1, h - 1);
            int r = (int)(from.getRed() + ratio * (to.getRed() - from.getRed()));
            int gr = (int)(from.getGreen() + ratio * (to.getGreen() - from.getGreen()));
            int b = (int)(from.getBlue() + ratio * (to.getBlue() - from.getBlue()));
            Color c = new Color(clamp(r), clamp(gr), clamp(b));
            for (int x = startX; x < startX + w && x < SIZE; x++) {
                img.setRGB(x, y, c.getRGB());
            }
        }
    }

    static int clamp(int val) {
        return Math.max(0, Math.min(255, val));
    }

    static void save(BufferedImage img, String path) throws Exception {
        File file = new File(path);
        file.getParentFile().mkdirs();
        ImageIO.write(img, "png", file);
    }
}
