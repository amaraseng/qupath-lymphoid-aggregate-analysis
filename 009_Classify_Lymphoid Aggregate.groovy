import qupath.lib.objects.classes.PathClass

def imageData = getCurrentImageData()
def server = imageData.getServer()
def cal = server.getPixelCalibration()

double pxW = cal.getPixelWidthMicrons()
double pxH = cal.getPixelHeightMicrons()

// Parameters for Lymphoid Aggregate classification
double minCD20FracOverall = 0.10

// Classes
def lymphoidAggClass = PathClass.fromString("Lymphoid Aggregate")
def aggClass = PathClass.fromString("Unclassified Aggregate")

// Pre-filter cells once: CD3/CD20 lymphocytes only
def lymphocyteCells = getCellObjects().findAll {
    def cls = it.getPathClass()?.getName()
    cls == "CD3" || cls == "CD20"
}

// Apply classification to all annotations
def annotations = getAnnotationObjects()

annotations.each { ann ->

    def roi = ann.getROI()

    double bx = roi.getBoundsX()
    double by = roi.getBoundsY()
    double bw = roi.getBoundsWidth()
    double bh = roi.getBoundsHeight()

    int lymphocyteCount = 0
    int cd20Count = 0

    for (cell in lymphocyteCells) {
        def cellROI = cell.getROI()
        double x = cellROI.getCentroidX()
        double y = cellROI.getCentroidY()

        // Fast reject by bounding box
        if (x < bx || x > bx + bw || y < by || y > by + bh)
            continue

        if (!roi.contains(x, y))
            continue

        lymphocyteCount++

        if (cell.getPathClass()?.getName() == "CD20")
            cd20Count++
    }

    // Lymphoid Aggregate criteria
    if (lymphocyteCount > 0 &&
        (cd20Count / (double) lymphocyteCount) >= minCD20FracOverall) {
        ann.setPathClass(lymphoidAggClass)
    } else {
        ann.setPathClass(aggClass)
    }
}

// Summary
int lymphoidAggN = annotations.count { it.getPathClass()?.getName() == "Lymphoid Aggregate" }
int aggN = annotations.count { it.getPathClass()?.getName() == "Unclassified Aggregate" }

print "✅ Lymphoid aggregate classification complete"
print "Lymphoid Aggregate: ${lymphoidAggN}"
print "Unclassified Aggregate: ${aggN}"