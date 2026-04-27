import qupath.lib.objects.classes.PathClass

// Minimum TLS area from literature
double minAreaUm2 = 6245.0

def imageData = getCurrentImageData()
def server = imageData.getServer()
def cal = server.getPixelCalibration()

double pxW = cal.getPixelWidthMicrons()
double pxH = cal.getPixelHeightMicrons()

// Get all Aggregate annotations
def aggs = getAnnotationObjects().findAll {
    it.getPathClass()?.getName() == "Aggregate"
}

if (aggs.isEmpty()) {
    print "⚠️ No Aggregate annotations found"
    return
}

def toRemove = []

aggs.each { ann ->
    def roi = ann.getROI()
    double areaUm2 = roi.getScaledArea(pxW, pxH)

    if (areaUm2 < minAreaUm2) {
        toRemove << ann
    }
}

if (!toRemove.isEmpty()) {
    removeObjects(toRemove, true)
}

print "Aggregate annotations checked: ${aggs.size()}"
print "Removed aggregates < ${minAreaUm2} µm²: ${toRemove.size()}"