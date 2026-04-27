// QuPath 0.6.0
// Apply pixel classifier "Tissue_Detection" within all existing annotations
// Rename created Tissue annotation(s) to "Tissue"

def classifierName = "Tissue_Detection"
double minArea = 0.0
double minHoleArea = 0.0
def tissueClassName = "Tissue"
def newAnnotationName = "Tissue"

def annotations = getAnnotationObjects()

if (annotations.isEmpty()) {
    println "No annotations found."
    return
}

println "Found ${annotations.size()} annotation(s)."
selectObjects(annotations)

// Create annotations from pixel classifier
createAnnotationsFromPixelClassifier(classifierName, minArea, minHoleArea, "SELECT_NEW")

// Rename newly created Tissue annotations
def selectedAnnotations = getSelectedObjects().findAll { it.isAnnotation() }

int renamed = 0
selectedAnnotations.each { ann ->
    def pathClass = ann.getPathClass()
    if (pathClass != null && pathClass.getName() == tissueClassName) {
        ann.setName(newAnnotationName)
        renamed++
    }
}

fireHierarchyUpdate()
println "Done. Renamed ${renamed} annotation(s) to '${newAnnotationName}'."