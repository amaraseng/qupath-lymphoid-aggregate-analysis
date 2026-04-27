import qupath.lib.objects.PathObjects
import qupath.lib.objects.classes.PathClass
import qupath.lib.roi.ROIs
import qupath.lib.regions.ImagePlane

def imageData = getCurrentImageData()
def server = imageData.getServer()
def cal = server.getPixelCalibration()

double pxW = cal.getPixelWidthMicrons()
double pxH = cal.getPixelHeightMicrons()

// -------- Parameters --------
double tileSizeUm = 40.0
int minAggCells = 12
// ----------------------------

double tileW = tileSizeUm / pxW
double tileH = tileSizeUm / pxH

def plane = ImagePlane.getDefaultPlane()
def aggClass = PathClass.fromString("Aggregate")

int imgW = server.getWidth()
int imgH = server.getHeight()

int nTilesX = Math.ceil(imgW / tileW) as int
int nTilesY = Math.ceil(imgH / tileH) as int

// Delete old Aggregate annotations first
def oldAggs = getAnnotationObjects().findAll {
    it.getPathClass()?.getName() == "Aggregate"
}
if (!oldAggs.isEmpty()) {
    removeObjects(oldAggs, true)
}

// Map tile index → cell count
def tileCounts = [:].withDefault { 0 }

// Collect only CD3/CD20 cells once
def lymphs = getCellObjects().findAll {
    it.getPathClass()?.getName() in ["CD3", "CD20"]
}

if (lymphs.isEmpty()) {
    print "⚠️ No CD3/CD20 lymphocytes found"
    return
}

// Assign each cell to its tile
lymphs.each { cell ->
    def roi = cell.getROI()
    int tx = (roi.getCentroidX() / tileW) as int
    int ty = (roi.getCentroidY() / tileH) as int

    if (tx >= 0 && tx < nTilesX && ty >= 0 && ty < nTilesY) {
        tileCounts["${tx}_${ty}"]++
    }
}

// Create aggregate tiles
def tiles = []

tileCounts.each { key, count ->
    if (count >= minAggCells) {
        def parts = key.split("_")
        int tx = parts[0] as int
        int ty = parts[1] as int

        double x = tx * tileW
        double y = ty * tileH

        def roi = ROIs.createRectangleROI(x, y, tileW, tileH, plane)
        tiles << PathObjects.createAnnotationObject(roi, aggClass)
    }
}

addObjects(tiles)

print "✅ Fast aggregate tile detection complete"
print "Old Aggregate annotations removed: ${oldAggs.size()}"
print "Aggregate tiles detected: ${tiles.size()}"