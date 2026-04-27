import qupath.lib.roi.interfaces.ROI

// =====================================================
// Delete detections inside any Ignore* annotations
// QuPath 0.6.0
// Works on current image, and with Run for project
// =====================================================

// ---------------------
// User settings
// ---------------------
def ignorePrefix = 'Ignore'   // matches Ignore, Ignore1, Ignore_debris, etc.
// ---------------------

def imageData = getCurrentImageData()
if (imageData == null) {
    println "❌ No image open."
    return
}

def hierarchy = imageData.getHierarchy()
def imageName = imageData.getServer().getMetadata().getName()

def ignoreAnnotations = hierarchy.getAnnotationObjects().findAll {
    def pc = it.getPathClass()
    pc != null && pc.getName() != null && pc.getName().startsWith(ignorePrefix)
}

def detections = hierarchy.getDetectionObjects()

println "📂 Processing image: ${imageName}"
println "Found ${ignoreAnnotations.size()} Ignore* annotation(s)"
println "Found ${detections.size()} detection(s)"

if (ignoreAnnotations.isEmpty()) {
    println "⚠️ No Ignore* annotations found. Nothing to do."
    return
}

if (detections.isEmpty()) {
    println "⚠️ No detections found. Nothing to do."
    return
}

// Store Ignore ROIs
def ignoreROIs = ignoreAnnotations.collect { it.getROI() }.findAll { it != null && !it.isEmpty() }

if (ignoreROIs.isEmpty()) {
    println "⚠️ Ignore ROIs are empty. Nothing to do."
    return
}

def detectionsToDelete = []
int checked = 0

for (det in detections) {
    def roi = det.getROI()
    if (roi == null)
        continue

    double cx = roi.getCentroidX()
    double cy = roi.getCentroidY()
    int z = roi.getZ()
    int t = roi.getT()

    boolean insideIgnore = false
    for (ROI ignoreROI in ignoreROIs) {
        if (ignoreROI.getZ() == z && ignoreROI.getT() == t && ignoreROI.contains(cx, cy)) {
            insideIgnore = true
            break
        }
    }

    if (insideIgnore)
        detectionsToDelete << det

    checked++
    if (checked % 200000 == 0)
        println "Checked ${checked} detections..."
}

println "Deleting ${detectionsToDelete.size()} detection(s) inside Ignore* annotations..."

if (!detectionsToDelete.isEmpty()) {
    hierarchy.removeObjects(detectionsToDelete, true)
}

fireHierarchyUpdate()

def projectEntry = getProjectEntry()
if (projectEntry != null) {
    projectEntry.saveImageData(imageData)
    println "💾 Saved: ${imageName}"
}

println "✅ Done."