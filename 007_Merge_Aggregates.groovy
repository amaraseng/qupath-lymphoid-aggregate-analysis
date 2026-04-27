import qupath.lib.objects.PathObjects
import qupath.lib.objects.classes.PathClass
import qupath.lib.roi.ROIs
import qupath.lib.roi.RoiTools
import qupath.lib.regions.ImagePlane
import qupath.lib.roi.interfaces.ROI

def plane = ImagePlane.getDefaultPlane()
def aggClass = PathClass.fromString("Aggregate")

// Tolerance in pixels for edge alignment
double eps = 1.0

// Get Aggregate tiles
def tiles = getAnnotationObjects().findAll {
    it.getPathClass()?.getName() == "Aggregate"
}

if (tiles.isEmpty()) {
    print "⚠️ No Aggregate tiles found to merge"
    return
}

// --- Edge-touch only test (NO corner-only merges) ---
boolean touch(r1, r2, eps) {

    double x1 = r1.getBoundsX()
    double y1 = r1.getBoundsY()
    double x2 = x1 + r1.getBoundsWidth()
    double y2 = y1 + r1.getBoundsHeight()

    double a1 = r2.getBoundsX()
    double b1 = r2.getBoundsY()
    double a2 = a1 + r2.getBoundsWidth()
    double b2 = b1 + r2.getBoundsHeight()

    // Overlap lengths
    double xOverlap = Math.min(x2, a2) - Math.max(x1, a1)
    double yOverlap = Math.min(y2, b2) - Math.max(y1, b1)

    // Horizontal edge touch (top/bottom)
    boolean horizontalTouch =
        (Math.abs(y2 - b1) <= eps || Math.abs(b2 - y1) <= eps) &&
        xOverlap > eps

    // Vertical edge touch (left/right)
    boolean verticalTouch =
        (Math.abs(x2 - a1) <= eps || Math.abs(a2 - x1) <= eps) &&
        yOverlap > eps

    return horizontalTouch || verticalTouch
}

def remaining = new ArrayList<>(tiles)
def merged = []

while (!remaining.isEmpty()) {

    def cluster = [remaining.remove(0)]
    boolean added

    do {
        added = false
        def toAdd = []

        remaining.each { r ->
            if (cluster.any { c -> touch(c.getROI(), r.getROI(), eps) }) {
                toAdd << r
                added = true
            }
        }

        remaining.removeAll(toAdd)
        cluster.addAll(toAdd)

    } while (added)

    // ---- Polygon union of cluster ----
    ROI unionROI = cluster[0].getROI()
    for (int i = 1; i < cluster.size(); i++) {
        unionROI = RoiTools.union(unionROI, cluster[i].getROI())
    }

    def ann = PathObjects.createAnnotationObject(unionROI, aggClass)
    ann.setName("Aggregate")
    merged << ann
}

// Replace tiles with merged polygon aggregates
removeObjects(tiles, false)
addObjects(merged)

fireHierarchyUpdate()

print "✅ Polygon-union Aggregate merge complete"
print "Merged aggregates: ${merged.size()}"

// Save project changes
def project = getProject()
if (project != null) {
    project.syncChanges()
    print "💾 Project saved"
}
