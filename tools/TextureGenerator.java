import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.File;

/**
 * Generates 32x32 bird textures with accurate colours from ornithological research.
 *
 * Minecraft cuboid UV mapping: for a box at texOffs(u,v) with dimensions (w,h,d):
 *   Top face:    (u+d,     v,       w, d)
 *   Bottom face: (u+d+w,   v,       w, d)
 *   Front face:  (u+d,     v+d,     w, h)
 *   Back face:   (u+d+w+d, v+d,     w, h)
 *   Left face:   (u,       v+d,     d, h)
 *   Right face:  (u+d+w,   v+d,     d, h)
 */
public class TextureGenerator {

    static final int SIZE = 32;

    public static void main(String[] args) throws Exception {
        String basePath = "src/main/resources/assets/britishbirds/textures/entity";

        generateRobin(basePath + "/robin/robin.png");
        generateRobinJuvenile(basePath + "/robin/robin_baby.png");
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
    // Body: texOffs(0,0), 4x5x5
    // Head: texOffs(0,10), 3x3x3
    // Beak: texOffs(12,10), 1x1x1
    // Wings: texOffs(18,0), 1x3x4
    // Tail: texOffs(0,16), 2x1x3
    // Legs: texOffs(0,20), 1x3x1

    static void generateRobin(String path) throws Exception {
        BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();

        // Background transparent
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(0, 0, SIZE, SIZE);
        g.setComposite(AlphaComposite.SrcOver);

        Color oliveBrown = new Color(0x6B, 0x6B, 0x3A);       // back/crown
        Color orangeRed = new Color(0xD4, 0x60, 0x2A);         // breast
        Color offWhite = new Color(0xF0, 0xED, 0xE0);           // belly
        Color darkBrown = new Color(0x4A, 0x3B, 0x2A);          // wings/tail
        Color warmBuff = new Color(0xC4, 0xA8, 0x82);           // flanks
        Color blueGrey = new Color(0x8B, 0x9D, 0xAF);           // breast border
        Color pinkBrown = new Color(0xC4, 0xA8, 0x82);          // legs
        Color black = new Color(0x33, 0x22, 0x11);              // beak/eye
        Color eye = new Color(0x11, 0x11, 0x11);

        // Body: texOffs(0,0), w=4, h=5, d=5
        // Top: olive-brown (back viewed from above)
        fillBox(g, 0, 0, 4, 5, 5, oliveBrown, oliveBrown, oliveBrown, oliveBrown, orangeRed, oliveBrown);
        // Override front face with orange breast
        fillFace(g, 5, 5, 4, 5, orangeRed);  // front = breast
        // Override bottom with off-white belly
        fillFace(g, 9, 0, 4, 5, offWhite);   // bottom = belly
        // Left and right sides: upper olive, lower buff/orange transition
        for (int y = 5; y < 10; y++) {
            Color c = y < 7 ? oliveBrown : (y < 9 ? blueGrey : warmBuff);
            for (int x = 0; x < 5; x++) img.setRGB(x, y, c.getRGB());      // left side
            for (int x = 9; x < 14; x++) img.setRGB(x, y, c.getRGB());     // right side
        }

        // Head: texOffs(0,10), w=3, h=3, d=3
        // Top: olive-brown crown
        fillFace(g, 3, 10, 3, 3, oliveBrown);  // top
        fillFace(g, 6, 10, 3, 3, oliveBrown);  // bottom (chin area - orange)
        fillFace(g, 3, 13, 3, 3, orangeRed);   // front face - orange-red
        fillFace(g, 9, 13, 3, 3, oliveBrown);   // back - olive
        fillFace(g, 0, 13, 3, 3, oliveBrown);   // left - olive with orange patch
        fillFace(g, 6, 13, 3, 3, oliveBrown);   // right - olive with orange patch
        // Add eye spots on left and right faces
        img.setRGB(1, 14, eye.getRGB());  // left eye
        img.setRGB(7, 14, eye.getRGB());  // right eye
        // Add orange on sides near front
        img.setRGB(0, 14, orangeRed.getRGB());
        img.setRGB(0, 15, orangeRed.getRGB());
        img.setRGB(8, 14, orangeRed.getRGB());
        img.setRGB(8, 15, orangeRed.getRGB());
        // Orange chin
        fillFace(g, 6, 10, 3, 3, orangeRed);

        // Beak: texOffs(12,10), 1x1x1 - dark
        fillBox(g, 12, 10, 1, 1, 1, black, black, black, black, black, black);

        // Wings: texOffs(18,0), w=1, h=3, d=4  - dark brown with olive edge
        fillBox(g, 18, 0, 1, 3, 4, darkBrown, darkBrown, darkBrown, darkBrown, darkBrown, darkBrown);
        // Add subtle olive fringe on top edge
        for (int x = 22; x < 23; x++) {
            img.setRGB(x, 0, oliveBrown.getRGB());
            img.setRGB(x, 1, oliveBrown.getRGB());
        }

        // Tail: texOffs(0,16), w=2, h=1, d=3 - dark brown
        fillBox(g, 0, 16, 2, 1, 3, darkBrown, darkBrown, darkBrown, darkBrown, darkBrown, darkBrown);

        // Legs: texOffs(0,20), w=1, h=3, d=1 - pinkish brown
        fillBox(g, 0, 20, 1, 3, 1, pinkBrown, pinkBrown, pinkBrown, pinkBrown, pinkBrown, pinkBrown);

        g.dispose();
        save(img, path);
        System.out.println("Generated: " + path);
    }

    static void generateRobinJuvenile(String path) throws Exception {
        BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(0, 0, SIZE, SIZE);
        g.setComposite(AlphaComposite.SrcOver);

        Color speckledBrown = new Color(0x8B, 0x73, 0x55);
        Color darkSpeckle = new Color(0x6B, 0x5B, 0x3A);
        Color goldenBuff = new Color(0xA8, 0x90, 0x70);
        Color pinkBrown = new Color(0xC4, 0xA8, 0x82);
        Color black = new Color(0x33, 0x22, 0x11);
        Color eye = new Color(0x11, 0x11, 0x11);

        // Fill everything with speckled brown pattern (no orange!)
        fillBox(g, 0, 0, 4, 5, 5, speckledBrown, speckledBrown, goldenBuff, speckledBrown, speckledBrown, speckledBrown);
        // Add speckles
        addSpeckles(img, 0, 0, 18, 10, darkSpeckle, goldenBuff);

        // Head: speckled
        fillBox(g, 0, 10, 3, 3, 3, speckledBrown, speckledBrown, speckledBrown, speckledBrown, speckledBrown, speckledBrown);
        addSpeckles(img, 0, 10, 12, 6, darkSpeckle, goldenBuff);
        img.setRGB(1, 14, eye.getRGB());
        img.setRGB(7, 14, eye.getRGB());

        // Beak
        fillBox(g, 12, 10, 1, 1, 1, black, black, black, black, black, black);

        // Wings: darker brown
        fillBox(g, 18, 0, 1, 3, 4, darkSpeckle, darkSpeckle, darkSpeckle, darkSpeckle, darkSpeckle, darkSpeckle);

        // Tail
        fillBox(g, 0, 16, 2, 1, 3, darkSpeckle, darkSpeckle, darkSpeckle, darkSpeckle, darkSpeckle, darkSpeckle);

        // Legs
        fillBox(g, 0, 20, 1, 3, 1, pinkBrown, pinkBrown, pinkBrown, pinkBrown, pinkBrown, pinkBrown);

        g.dispose();
        save(img, path);
        System.out.println("Generated: " + path);
    }

    // === BARN OWL ===
    // Body: texOffs(0,0), 5x6x6
    // Head: texOffs(0,12), 4x4x4
    // Facial disc: texOffs(16,12), 5x4x1
    // Beak: texOffs(16,17), 1x1x1
    // Wings: texOffs(0,20), 1x5x7
    // Tail: texOffs(16,20), 3x1x2
    // Legs: texOffs(22,0), 1x4x1

    static void generateBarnOwlMale(String path) throws Exception {
        BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(0, 0, SIZE, SIZE);
        g.setComposite(AlphaComposite.SrcOver);

        Color goldenBuff = new Color(0xC4, 0xA0, 0x55);      // back
        Color white = new Color(0xF5, 0xF0, 0xE0);             // breast (male - very white)
        Color cream = new Color(0xF0, 0xE8, 0xD0);             // face disc
        Color darkEye = new Color(0x11, 0x11, 0x11);            // eyes (dark, not yellow!)
        Color paleHorn = new Color(0xD4, 0xC0, 0xA0);           // beak
        Color greyPink = new Color(0xB0, 0xA0, 0x90);           // legs/feet
        Color buffRim = new Color(0xA0, 0x85, 0x60);            // facial disc rim
        Color tawnyOrange = new Color(0xB8, 0x8A, 0x48);        // back vermiculation
        Color darkSpot = new Color(0x4A, 0x3B, 0x2A);           // dorsal spots

        // Body: golden-buff back, white front
        fillBox(g, 0, 0, 5, 6, 6, goldenBuff, goldenBuff, white, goldenBuff, goldenBuff, goldenBuff);
        // Override front face with white breast
        fillFace(g, 6, 6, 5, 6, white);
        // Override bottom with white belly
        fillFace(g, 11, 0, 5, 6, white);
        // Add dark spots on back/top
        addSpeckles(img, 6, 0, 5, 6, darkSpot, tawnyOrange);
        // Sides: golden upper, white lower transition
        for (int y = 6; y < 12; y++) {
            Color c = y < 9 ? goldenBuff : white;
            for (int x = 0; x < 6; x++) img.setRGB(x, y, c.getRGB());
            for (int x = 11; x < 17; x++) img.setRGB(x, y, c.getRGB());
        }

        // Head: golden-buff
        fillBox(g, 0, 12, 4, 4, 4, goldenBuff, goldenBuff, cream, goldenBuff, goldenBuff, goldenBuff);

        // Facial disc: cream/white heart shape
        fillBox(g, 16, 12, 5, 4, 1, cream, cream, cream, cream, cream, cream);
        // Add buff rim around edges
        for (int x = 16; x < 27; x++) {
            if (x < 17 || x > 25) continue;
            img.setRGB(x, 12, buffRim.getRGB());  // top rim
            img.setRGB(x, 16, buffRim.getRGB());  // bottom rim
        }
        // Add dark eyes on facial disc front face (at 17,13+1 offset)
        img.setRGB(18, 14, darkEye.getRGB());  // left eye
        img.setRGB(20, 14, darkEye.getRGB());  // right eye

        // Beak: pale horn
        fillBox(g, 16, 17, 1, 1, 1, paleHorn, paleHorn, paleHorn, paleHorn, paleHorn, paleHorn);

        // Wings: golden-buff with barring
        fillBox(g, 0, 20, 1, 5, 7, goldenBuff, goldenBuff, goldenBuff, goldenBuff, goldenBuff, goldenBuff);
        // Add dark bars on wing
        for (int y = 22; y < 25; y += 2) {
            for (int x = 1; x < 8; x++) {
                img.setRGB(x, y, tawnyOrange.getRGB());
            }
        }

        // Tail: golden-buff with dark bars
        fillBox(g, 16, 20, 3, 1, 2, goldenBuff, goldenBuff, goldenBuff, goldenBuff, goldenBuff, goldenBuff);

        // Legs: grey-pink, feathered
        fillBox(g, 22, 0, 1, 4, 1, greyPink, greyPink, greyPink, greyPink, white, greyPink);

        g.dispose();
        save(img, path);
        System.out.println("Generated: " + path);
    }

    static void generateBarnOwlFemale(String path) throws Exception {
        // Female is similar but with buff wash and more spots on breast
        BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(0, 0, SIZE, SIZE);
        g.setComposite(AlphaComposite.SrcOver);

        Color goldenBuff = new Color(0xC4, 0xA0, 0x55);
        Color buffWhite = new Color(0xE8, 0xDD, 0xC8);        // female breast - more buff
        Color cream = new Color(0xF0, 0xE8, 0xD0);
        Color darkEye = new Color(0x11, 0x11, 0x11);
        Color paleHorn = new Color(0xD4, 0xC0, 0xA0);
        Color greyPink = new Color(0xB0, 0xA0, 0x90);
        Color buffRim = new Color(0xA0, 0x85, 0x60);
        Color tawnyOrange = new Color(0xB8, 0x8A, 0x48);
        Color darkSpot = new Color(0x4A, 0x3B, 0x2A);

        // Body: similar to male but buffier breast with spots
        fillBox(g, 0, 0, 5, 6, 6, goldenBuff, goldenBuff, buffWhite, goldenBuff, goldenBuff, goldenBuff);
        fillFace(g, 6, 6, 5, 6, buffWhite);
        fillFace(g, 11, 0, 5, 6, buffWhite);
        addSpeckles(img, 6, 0, 5, 6, darkSpot, tawnyOrange);
        // Add spots on breast (female characteristic)
        addSpeckles(img, 6, 6, 5, 6, darkSpot, buffWhite);
        for (int y = 6; y < 12; y++) {
            Color c = y < 9 ? goldenBuff : buffWhite;
            for (int x = 0; x < 6; x++) img.setRGB(x, y, c.getRGB());
            for (int x = 11; x < 17; x++) img.setRGB(x, y, c.getRGB());
        }

        // Head, facial disc, beak, wings, tail, legs - same as male
        fillBox(g, 0, 12, 4, 4, 4, goldenBuff, goldenBuff, cream, goldenBuff, goldenBuff, goldenBuff);
        fillBox(g, 16, 12, 5, 4, 1, cream, cream, cream, cream, cream, cream);
        img.setRGB(18, 14, darkEye.getRGB());
        img.setRGB(20, 14, darkEye.getRGB());
        fillBox(g, 16, 17, 1, 1, 1, paleHorn, paleHorn, paleHorn, paleHorn, paleHorn, paleHorn);
        fillBox(g, 0, 20, 1, 5, 7, goldenBuff, goldenBuff, goldenBuff, goldenBuff, goldenBuff, goldenBuff);
        fillBox(g, 16, 20, 3, 1, 2, goldenBuff, goldenBuff, goldenBuff, goldenBuff, goldenBuff, goldenBuff);
        fillBox(g, 22, 0, 1, 4, 1, greyPink, greyPink, greyPink, greyPink, buffWhite, greyPink);

        g.dispose();
        save(img, path);
        System.out.println("Generated: " + path);
    }

    // === PEREGRINE FALCON ===
    // Body: texOffs(0,0), 5x5x7
    // Head: texOffs(0,12), 3x3x3
    // Beak: texOffs(12,12), 1x1x2
    // Wings: texOffs(0,18), 1x4x8
    // Tail: texOffs(18,0), 3x1x3
    // Legs: texOffs(24,0), 1x3x1

    static void generatePeregrineAdult(String path) throws Exception {
        BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(0, 0, SIZE, SIZE);
        g.setComposite(AlphaComposite.SrcOver);

        Color slateGrey = new Color(0x4A, 0x55, 0x68);        // back - blue-grey slate
        Color darkSlate = new Color(0x3A, 0x42, 0x52);         // darker slate
        Color paleBarred = new Color(0xE8, 0xE0, 0xD5);        // breast - pale cream
        Color darkBarring = new Color(0x55, 0x4A, 0x3E);       // breast bars
        Color blackHead = new Color(0x1A, 0x1A, 0x22);         // dark helmet/cap
        Color whiteCheek = new Color(0xF0, 0xEC, 0xE5);        // pale cheek/throat
        Color yellowCere = new Color(0xDA, 0xA5, 0x20);        // cere/eye-ring
        Color yellowFeet = new Color(0xDA, 0xA5, 0x20);        // feet
        Color darkBeak = new Color(0x2A, 0x2A, 0x33);          // beak
        Color darkEye = new Color(0x11, 0x11, 0x11);

        // Body: slate back, pale barred front
        fillBox(g, 0, 0, 5, 5, 7, slateGrey, slateGrey, paleBarred, slateGrey, slateGrey, slateGrey);
        // Front: pale with horizontal dark bars
        for (int y = 7; y < 12; y++) {
            Color c = (y % 2 == 0) ? paleBarred : darkBarring;
            for (int x = 7; x < 12; x++) img.setRGB(x, y, c.getRGB());
        }
        // Bottom: pale
        fillFace(g, 12, 0, 5, 7, paleBarred);
        // Sides: slate upper, barred lower
        for (int y = 7; y < 12; y++) {
            Color c = y < 9 ? slateGrey : ((y % 2 == 0) ? paleBarred : darkBarring);
            for (int x = 0; x < 7; x++) img.setRGB(x, y, c.getRGB());
            for (int x = 12; x < 19; x++) img.setRGB(x, y, c.getRGB());
        }
        // Add subtle barring on back
        for (int y = 0; y < 7; y++) {
            if (y % 3 == 0) {
                for (int x = 7; x < 12; x++) img.setRGB(x, y, darkSlate.getRGB());
            }
        }

        // Head: dark helmet with white cheek and moustachial stripe
        fillBox(g, 0, 12, 3, 3, 3, blackHead, blackHead, blackHead, blackHead, blackHead, blackHead);
        // Front face: dark top (helmet), white cheek area, dark moustache
        fillFace(g, 3, 15, 3, 3, blackHead);  // front - dark cap
        img.setRGB(3, 16, whiteCheek.getRGB());  // left cheek
        img.setRGB(5, 16, whiteCheek.getRGB());  // right cheek
        img.setRGB(4, 17, whiteCheek.getRGB());  // throat
        // Eyes
        img.setRGB(1, 16, darkEye.getRGB());  // left eye
        img.setRGB(7, 16, darkEye.getRGB());  // right eye
        // White throat on sides
        img.setRGB(0, 17, whiteCheek.getRGB());
        img.setRGB(8, 17, whiteCheek.getRGB());

        // Beak: dark with yellow cere base
        fillBox(g, 12, 12, 1, 1, 2, darkBeak, darkBeak, darkBeak, darkBeak, darkBeak, darkBeak);
        img.setRGB(14, 13, yellowCere.getRGB());  // cere

        // Wings: dark slate-grey
        fillBox(g, 0, 18, 1, 4, 8, slateGrey, slateGrey, slateGrey, slateGrey, slateGrey, slateGrey);
        // Darker wingtips
        for (int x = 6; x < 9; x++) {
            for (int y = 19; y < 22; y++) img.setRGB(x, y, darkSlate.getRGB());
        }

        // Tail: slate with dark bars
        fillBox(g, 18, 0, 3, 1, 3, slateGrey, slateGrey, slateGrey, slateGrey, slateGrey, slateGrey);
        // Dark subterminal band
        for (int x = 21; x < 24; x++) img.setRGB(x, 1, darkSlate.getRGB());

        // Legs: yellow
        fillBox(g, 24, 0, 1, 3, 1, yellowFeet, yellowFeet, yellowFeet, yellowFeet, yellowFeet, yellowFeet);

        g.dispose();
        save(img, path);
        System.out.println("Generated: " + path);
    }

    static void generatePeregrineJuvenile(String path) throws Exception {
        BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(0, 0, SIZE, SIZE);
        g.setComposite(AlphaComposite.SrcOver);

        // Juvenile: brown (not grey), streaked (not barred) underparts
        Color brown = new Color(0x6B, 0x55, 0x3A);
        Color buffCream = new Color(0xE0, 0xD0, 0xB5);
        Color darkStreak = new Color(0x4A, 0x3B, 0x2A);
        Color darkHead = new Color(0x3A, 0x2E, 0x22);
        Color buffCheek = new Color(0xD0, 0xC0, 0xA5);
        Color blueGreyFeet = new Color(0x7A, 0x85, 0x90);
        Color darkBeak = new Color(0x2A, 0x2A, 0x33);
        Color darkEye = new Color(0x11, 0x11, 0x11);

        // Body: brown back, streaked buff front
        fillBox(g, 0, 0, 5, 5, 7, brown, brown, buffCream, brown, brown, brown);
        // Add vertical streaks on front (not horizontal bars like adult)
        for (int x = 7; x < 12; x++) {
            if (x % 2 == 0) {
                for (int y = 7; y < 12; y++) img.setRGB(x, y, darkStreak.getRGB());
            }
        }
        fillFace(g, 12, 0, 5, 7, buffCream);

        // Head: brown (not black like adult)
        fillBox(g, 0, 12, 3, 3, 3, darkHead, darkHead, darkHead, darkHead, darkHead, darkHead);
        img.setRGB(3, 16, buffCheek.getRGB());
        img.setRGB(5, 16, buffCheek.getRGB());
        img.setRGB(4, 17, buffCheek.getRGB());
        img.setRGB(1, 16, darkEye.getRGB());
        img.setRGB(7, 16, darkEye.getRGB());

        fillBox(g, 12, 12, 1, 1, 2, darkBeak, darkBeak, darkBeak, darkBeak, darkBeak, darkBeak);
        fillBox(g, 0, 18, 1, 4, 8, brown, brown, brown, brown, brown, brown);
        fillBox(g, 18, 0, 3, 1, 3, brown, brown, brown, brown, brown, brown);
        fillBox(g, 24, 0, 1, 3, 1, blueGreyFeet, blueGreyFeet, blueGreyFeet, blueGreyFeet, blueGreyFeet, blueGreyFeet);

        g.dispose();
        save(img, path);
        System.out.println("Generated: " + path);
    }

    // === MALLARD ===
    // Body: texOffs(0,0), 6x5x8
    // Head: texOffs(0,13), 3x3x3
    // Bill: texOffs(12,13), 2x1x2
    // Wings: texOffs(0,19), 1x4x6
    // Tail: texOffs(20,0), 4x1x3
    // Tail curl: texOffs(20,4), 1x1x2
    // Legs: texOffs(14,19), 1x3x1
    // Feet: texOffs(14,23), 2x1x2

    static void generateMallardMale(String path) throws Exception {
        BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(0, 0, SIZE, SIZE);
        g.setComposite(AlphaComposite.SrcOver);

        Color iridGreen = new Color(0x2D, 0x6B, 0x33);       // head - iridescent green
        Color chestnut = new Color(0x8B, 0x45, 0x13);         // breast - rich chestnut
        Color greyFlanks = new Color(0xB0, 0xAA, 0x9E);       // flanks - pale grey
        Color blackRump = new Color(0x1A, 0x1A, 0x1A);        // rump/undertail
        Color whiteRing = new Color(0xF0, 0xF0, 0xF0);        // neck ring
        Color yellowBill = new Color(0xDA, 0xA5, 0x20);       // bill
        Color orangeLegs = new Color(0xFF, 0x8C, 0x00);       // legs
        Color speculum = new Color(0x6A, 0x5A, 0xCD);         // wing speculum blue-purple
        Color speculumWhite = new Color(0xF0, 0xF0, 0xF0);    // speculum border
        Color greyBrown = new Color(0x8A, 0x7E, 0x72);        // back
        Color darkEye = new Color(0x11, 0x11, 0x11);

        // Body: grey-brown back, chestnut front (breast), grey flanks
        fillBox(g, 0, 0, 6, 5, 8, greyBrown, greyBrown, chestnut, greyBrown, greyBrown, greyBrown);
        // Bottom: grey (belly)
        fillFace(g, 14, 0, 6, 8, greyFlanks);
        // Sides: chestnut upper breast transitioning to grey flanks
        for (int y = 8; y < 13; y++) {
            Color c = y < 10 ? chestnut : greyFlanks;
            for (int x = 0; x < 8; x++) img.setRGB(x, y, c.getRGB());
            for (int x = 14; x < 22; x++) img.setRGB(x, y, c.getRGB());
        }
        // Back: add black rump area at tail end
        for (int x = 8; x < 14; x++) {
            img.setRGB(x, 0, blackRump.getRGB());
            img.setRGB(x, 1, blackRump.getRGB());
        }

        // Head: iridescent green
        fillBox(g, 0, 13, 3, 3, 3, iridGreen, iridGreen, iridGreen, iridGreen, iridGreen, iridGreen);
        // White neck ring on bottom of head
        fillFace(g, 6, 13, 3, 3, iridGreen);  // bottom - but add white ring
        for (int x = 6; x < 9; x++) img.setRGB(x, 13, whiteRing.getRGB());
        // Eyes
        img.setRGB(1, 15, darkEye.getRGB());
        img.setRGB(7, 15, darkEye.getRGB());

        // Bill: yellow
        fillBox(g, 12, 13, 2, 1, 2, yellowBill, yellowBill, yellowBill, yellowBill, yellowBill, yellowBill);
        // Black nail at tip
        img.setRGB(16, 14, new Color(0x1A, 0x1A, 0x1A).getRGB());

        // Wings: grey-brown with blue-purple speculum and white bars
        fillBox(g, 0, 19, 1, 4, 6, greyBrown, greyBrown, greyBrown, greyBrown, greyBrown, greyBrown);
        // Add speculum stripe on wing (on the outer face)
        for (int y = 21; y < 23; y++) {
            img.setRGB(1, y, speculumWhite.getRGB());
            img.setRGB(2, y, speculum.getRGB());
            img.setRGB(3, y, speculum.getRGB());
            img.setRGB(4, y, speculumWhite.getRGB());
        }

        // Tail: pale grey outer, black center
        fillBox(g, 20, 0, 4, 1, 3, greyFlanks, greyFlanks, greyFlanks, greyFlanks, blackRump, greyFlanks);

        // Tail curl: black
        fillBox(g, 20, 4, 1, 1, 2, blackRump, blackRump, blackRump, blackRump, blackRump, blackRump);

        // Legs: orange
        fillBox(g, 14, 19, 1, 3, 1, orangeLegs, orangeLegs, orangeLegs, orangeLegs, orangeLegs, orangeLegs);

        // Feet: orange webbed
        fillBox(g, 14, 23, 2, 1, 2, orangeLegs, orangeLegs, orangeLegs, orangeLegs, orangeLegs, orangeLegs);

        g.dispose();
        save(img, path);
        System.out.println("Generated: " + path);
    }

    static void generateMallardFemale(String path) throws Exception {
        BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(0, 0, SIZE, SIZE);
        g.setComposite(AlphaComposite.SrcOver);

        Color mottledBrown = new Color(0x8B, 0x73, 0x55);
        Color darkBrown = new Color(0x5A, 0x48, 0x30);
        Color buffEdge = new Color(0xC4, 0xAA, 0x82);
        Color darkCrown = new Color(0x4A, 0x3B, 0x2A);
        Color buffFace = new Color(0xD0, 0xBB, 0x95);
        Color darkEyeStripe = new Color(0x3A, 0x2E, 0x22);
        Color orangeBill = new Color(0xC0, 0x80, 0x40);        // mottled orange-brown bill
        Color orangeLegs = new Color(0xE0, 0x80, 0x20);
        Color speculum = new Color(0x6A, 0x5A, 0xCD);
        Color speculumWhite = new Color(0xF0, 0xF0, 0xF0);
        Color darkEye = new Color(0x11, 0x11, 0x11);

        // Body: mottled brown all over (cryptic)
        fillBox(g, 0, 0, 6, 5, 8, mottledBrown, mottledBrown, mottledBrown, mottledBrown, mottledBrown, mottledBrown);
        addSpeckles(img, 0, 0, 28, 13, darkBrown, buffEdge);

        // Head: dark crown, buff cheeks with dark eye stripe
        fillBox(g, 0, 13, 3, 3, 3, darkCrown, darkCrown, buffFace, darkCrown, darkCrown, darkCrown);
        // Eye stripe on sides
        img.setRGB(1, 15, darkEyeStripe.getRGB());
        img.setRGB(7, 15, darkEyeStripe.getRGB());
        img.setRGB(0, 15, darkEyeStripe.getRGB());
        img.setRGB(8, 15, darkEyeStripe.getRGB());
        // Buff face
        img.setRGB(0, 16, buffFace.getRGB());
        img.setRGB(8, 16, buffFace.getRGB());
        // Eyes
        img.setRGB(1, 15, darkEye.getRGB());
        img.setRGB(7, 15, darkEye.getRGB());

        // Bill: mottled orange-brown
        fillBox(g, 12, 13, 2, 1, 2, orangeBill, orangeBill, orangeBill, orangeBill, orangeBill, orangeBill);

        // Wings: brown with speculum
        fillBox(g, 0, 19, 1, 4, 6, mottledBrown, mottledBrown, mottledBrown, mottledBrown, mottledBrown, mottledBrown);
        for (int y = 21; y < 23; y++) {
            img.setRGB(1, y, speculumWhite.getRGB());
            img.setRGB(2, y, speculum.getRGB());
            img.setRGB(3, y, speculum.getRGB());
            img.setRGB(4, y, speculumWhite.getRGB());
        }

        // Tail
        fillBox(g, 20, 0, 4, 1, 3, mottledBrown, mottledBrown, mottledBrown, mottledBrown, mottledBrown, mottledBrown);
        // No tail curl for female
        fillBox(g, 20, 4, 1, 1, 2, mottledBrown, mottledBrown, mottledBrown, mottledBrown, mottledBrown, mottledBrown);

        // Legs
        fillBox(g, 14, 19, 1, 3, 1, orangeLegs, orangeLegs, orangeLegs, orangeLegs, orangeLegs, orangeLegs);
        fillBox(g, 14, 23, 2, 1, 2, orangeLegs, orangeLegs, orangeLegs, orangeLegs, orangeLegs, orangeLegs);

        g.dispose();
        save(img, path);
        System.out.println("Generated: " + path);
    }

    static void generateMallardDuckling(String path) throws Exception {
        BufferedImage img = new BufferedImage(SIZE, SIZE, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = img.createGraphics();
        g.setComposite(AlphaComposite.Clear);
        g.fillRect(0, 0, SIZE, SIZE);
        g.setComposite(AlphaComposite.SrcOver);

        Color yellow = new Color(0xFF, 0xD7, 0x00);           // bright yellow
        Color darkBack = new Color(0x3A, 0x2E, 0x22);          // dark brown back
        Color darkStripe = new Color(0x4A, 0x3B, 0x2A);        // eye stripe
        Color greyBill = new Color(0x55, 0x55, 0x55);
        Color greyLegs = new Color(0x55, 0x55, 0x55);
        Color darkEye = new Color(0x11, 0x11, 0x11);

        // Body: dark back, yellow belly/front
        fillBox(g, 0, 0, 6, 5, 8, darkBack, darkBack, yellow, darkBack, yellow, darkBack);
        // Add yellow spots on dark back (4 spots pattern)
        img.setRGB(9, 1, yellow.getRGB());
        img.setRGB(12, 1, yellow.getRGB());
        img.setRGB(9, 4, yellow.getRGB());
        img.setRGB(12, 4, yellow.getRGB());
        // Sides: yellow lower half
        for (int y = 8; y < 13; y++) {
            Color c = y < 10 ? darkBack : yellow;
            for (int x = 0; x < 8; x++) img.setRGB(x, y, c.getRGB());
            for (int x = 14; x < 22; x++) img.setRGB(x, y, c.getRGB());
        }

        // Head: yellow face, dark crown
        fillBox(g, 0, 13, 3, 3, 3, darkBack, darkBack, yellow, darkBack, yellow, darkBack);
        // Dark eye stripes
        img.setRGB(1, 15, darkEye.getRGB());
        img.setRGB(7, 15, darkEye.getRGB());
        img.setRGB(0, 15, darkStripe.getRGB());
        img.setRGB(8, 15, darkStripe.getRGB());

        // Bill: grey
        fillBox(g, 12, 13, 2, 1, 2, greyBill, greyBill, greyBill, greyBill, greyBill, greyBill);

        // Wings: dark
        fillBox(g, 0, 19, 1, 4, 6, darkBack, darkBack, darkBack, darkBack, darkBack, darkBack);

        // Tail
        fillBox(g, 20, 0, 4, 1, 3, darkBack, darkBack, darkBack, darkBack, darkBack, darkBack);
        fillBox(g, 20, 4, 1, 1, 2, darkBack, darkBack, darkBack, darkBack, darkBack, darkBack);

        // Legs: dark grey
        fillBox(g, 14, 19, 1, 3, 1, greyLegs, greyLegs, greyLegs, greyLegs, greyLegs, greyLegs);
        fillBox(g, 14, 23, 2, 1, 2, greyLegs, greyLegs, greyLegs, greyLegs, greyLegs, greyLegs);

        g.dispose();
        save(img, path);
        System.out.println("Generated: " + path);
    }

    // === HELPER METHODS ===

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

    static void save(BufferedImage img, String path) throws Exception {
        File file = new File(path);
        file.getParentFile().mkdirs();
        ImageIO.write(img, "png", file);
    }
}
