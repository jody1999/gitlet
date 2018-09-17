
/**
 * This class provides all code necessary to take a query box and produce
 * a query result. The getMapRaster method must return a Map containing all
 * seven of the required fields, otherwise the front end code will probably
 * not draw the output correctly.
 */
public class Rasterer {
    /**
     * The max image depth level.
     */
    public static final int MAX_DEPTH = 7;

    public static final double ULLON = -122.2998046875;
    public static final double ULLAT = 37.892195547244356;
    public static final double LRLON = -122.2119140625;
    public static final double LRLAT = 37.82280243352756;

    public static final double D0LONDPP = 0.00034332275390625;

    /**
     * Takes a user query and finds the grid of images that best matches the query. These images
     * will be combined into one big image (rastered) by the front end. The grid of images must obey
     * the following properties, where image in the grid is referred to as a "tile".
     * <ul>
     * <li>The tiles collected must cover the most longitudinal distance per pixel (LonDPP)
     * possible, while still covering less than or equal to the amount of longitudinal distance
     * per pixel in the query box for the user viewport size.</li>
     * <li>Contains all tiles that intersect the query bounding box that fulfill the above
     * condition.</li>
     * <li>The tiles must be arranged in-order to reconstruct the full image.</li>
     * </ul>
     *
     * @param params The RasterRequestParams containing coordinates of the query box and the browser
     *               viewport width and height.
     * @return A valid RasterResultParams containing the computed results.
     */

    public RasterResultParams getMapRaster(RasterRequestParams params) {
        System.out.print(params.ullon + "," + params.ullat + "," + params.w + "," + params.h + ","
                + params.lrlon + "," + params.lrlat);

        //1. get the lonDPP
        if (params.ullon < ULLON || params.ullat > ULLAT
                || params.lrlon > LRLON || params.lrlat < LRLAT) {
            System.out.println("queryFailed");
            return RasterResultParams.queryFailed();
        }
        params.lonDPP = lonDPP(params.lrlon, params.ullon, params.w);
        System.out.println("lonDPP " + params.lonDPP);

        //2. create a list of lonDPP and find the D
        int D = -1;
        for (int i = 0; i < 8; i++) {
            if (params.lonDPP >= D0LONDPP / Math.pow(2.0, i)) {
                D = i;
                break;
            }
        }
        if (D < 0) {
            D = 7;
        }
        //3. find out the tiles we need
        System.out.println("step 3 ");
        double unitLon = (LRLON - ULLON) / Math.pow(2, D);
        double unitLat = (LRLAT - ULLAT) / Math.pow(2, D);

        int ulx = (int) ((params.ullon - ULLON) / unitLon);
        int uly = (int) ((params.ullat - ULLAT) / unitLat);
        int lrx = (int) Math.pow(2, D) - 1 - (int) ((LRLON - params.lrlon) / unitLon);
        int lry = (int) Math.pow(2, D) - 1 - (int) ((LRLAT - params.lrlat) / unitLat);

        //4. finding the bounding box of every small box, put into a grid of filenames of images
        String[][] renderGrid = new String[lry - uly + 1][lrx - ulx + 1];
        for (int i = ulx; i < lrx + 1; i++) {
            for (int a = uly; a < lry + 1; a++) {
                renderGrid[a - uly][i - ulx] = "d" + D + "_x" + i + "_y" + a + ".png";
            }
        }

        //5. build the Result Object
        System.out.println("step 5");

        RasterResultParams resultParams = new RasterResultParams.Builder()
                .setRenderGrid(renderGrid)
                .setRasterUlLon(getULBoundingLon(ulx, D))
                .setRasterUlLat(getULBoundingLat(uly, D))
                .setRasterLrLon(getLRBoundingLon(lrx, D))
                .setRasterLrLat(getLRBoundingLat(lry, D))
                .setDepth(D)
                .setQuerySuccess(true)
                .create();

        System.out.print(resultParams);
        return resultParams;

    }


    /**
     * Calculates the lonDPP of an image or query box
     *
     * @param lrlon Lower right longitudinal value of the image or query box
     * @param ullon Upper left longitudinal value of the image or query box
     * @param width Width of the query box or image
     * @return lonDPP
     */
    private double lonDPP(double lrlon, double ullon, double width) {
        return (lrlon - ullon) / width;
    }

    private double getULBoundingLon(int x, int D) {
        return ULLON + (LRLON - ULLON) / Math.pow(2, D) * x;
    }

    private double getULBoundingLat(int y, int D) {
        return ULLAT + (LRLAT - ULLAT) / Math.pow(2, D) * y;
    }

    private double getLRBoundingLon(int x, int D) {
        return ULLON + (LRLON - ULLON) / Math.pow(2, D) * (x + 1);
    }

    private double getLRBoundingLat(int y, int D) {
        return ULLAT + (LRLAT - ULLAT) / Math.pow(2, D) * (y + 1);
    }
}
