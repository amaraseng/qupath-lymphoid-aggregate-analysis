import qupath.lib.gui.scripting.QPEx

// --------------------------------------------------
// Refresh cell detection for the CURRENT image only
// Compatible with QuPath 0.6.0
// Can also be used with "Run for project"
// Does NOT fill holes in Tissue
// --------------------------------------------------

// Get current image
def imageData = getCurrentImageData()
if (imageData == null) {
    println "❌ No image open."
    return
}

setImageType('FLUORESCENCE')

def hierarchy = imageData.getHierarchy()
def imageName = imageData.getServer().getMetadata().getName()

println "📂 Processing image: ${imageName}"

// Find "Tissue"
def annotation1 = hierarchy.getAnnotationObjects().find { it.getName() == "Tissue" }
if (annotation1 == null) {
    println "⚠️ 'Tissue' not found in ${imageName} - skipping."
    return
}

// Remove all existing detections
def existingDetections = hierarchy.getDetectionObjects()
if (!existingDetections.isEmpty()) {
    hierarchy.removeObjects(existingDetections, true)
    println "🧹 Removed ${existingDetections.size()} old detections"
}

// Select Tissue and run cell detection only inside it
selectObjects(annotation1)
runPlugin('qupath.imagej.detect.cells.WatershedCellDetection', '''
{
  "detectionImage": "Hoechst",
  "requestedPixelSizeMicrons": 0.2,
  "backgroundRadiusMicrons": 4.0,
  "backgroundByReconstruction": true,
  "medianRadiusMicrons": 0.0,
  "sigmaMicrons": 1.0,
  "minAreaMicrons": 2.0,
  "maxAreaMicrons": 400.0,
  "threshold": 45.0,
  "watershedPostProcess": true,
  "cellExpansionMicrons": 2.0,
  "includeNuclei": true,
  "smoothBoundaries": true,
  "makeMeasurements": true
}
''')
println "🔬 Re-ran cell detection in Tissue"

// Save if image belongs to a project
def projectEntry = getProjectEntry()
if (projectEntry != null) {
    projectEntry.saveImageData(imageData)
    println "💾 Saved: ${imageName}"
} else {
    println "ℹ️ Image not in a project - results not auto-saved to project."
}

println "🎉 Done: ${imageName}"