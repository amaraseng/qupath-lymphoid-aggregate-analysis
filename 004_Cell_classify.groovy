import qupath.lib.gui.scripting.QPEx

// --------------------------------------------------
// Run an object classifier on the CURRENT image only
// Compatible with QuPath 0.6.0
// Works with "Run for project"
// --------------------------------------------------

// -------- USER SETTING --------
def classifierName = "CD3_CD20"
// -----------------------------

// Get current image
def imageData = getCurrentImageData()
if (imageData == null) {
    println "❌ No image open."
    return
}

setImageType('FLUORESCENCE')

def hierarchy = imageData.getHierarchy()
def annotations = hierarchy.getAnnotationObjects()
def detections = hierarchy.getDetectionObjects()
def imageName = imageData.getServer().getMetadata().getName()

// Check annotations
if (annotations.isEmpty()) {
    println "⚠️ Skipping ${imageName}: no annotations found"
    return
}

// Check detections
if (detections.isEmpty()) {
    println "⚠️ Skipping ${imageName}: no detections found to classify"
    return
}

println "📂 Processing ${imageName} with ${annotations.size()} annotation(s) and ${detections.size()} detection(s)..."

// Run classifier on all detections
selectObjects(detections)
println "🔲 Classifying ${detections.size()} detections with '${classifierName}'"

runObjectClassifier(classifierName)

// Save if part of a project
def projectEntry = getProjectEntry()
if (projectEntry != null) {
    projectEntry.saveImageData(imageData)
    println "💾 Saved: ${imageName}"
} else {
    println "ℹ️ Image not in a project - results not auto-saved to project."
}

println "🎉 Done: ${imageName}"