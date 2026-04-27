import java.text.SimpleDateFormat
import java.util.Date
import java.io.File

def project = getProject()
if (project == null) {
    println "No project found."
    return
}

def dateString = new SimpleDateFormat("yyyy-MM-dd").format(new Date())

// Fix for QuPath returning a Path rather than a String
def projectDir = project.getPath().toAbsolutePath().toFile().getParentFile()

def resultsDir = new File(projectDir, "Results")
if (!resultsDir.exists())
    resultsDir.mkdirs()

def outFile = new File(resultsDir, "Annotation_Counts_${dateString}.csv")
def targetClasses = ["Lymphoid Aggregate"] as Set

outFile.withWriter("UTF-8") { writer ->
    writer.writeLine("Image,Classification,Count")

    for (entry in project.getImageList()) {
        def imageData = entry.readImageData()
        def imageName = imageData.getServer().getMetadata().getName()

        def counts = [
            "Lymphoid Aggregate": 0
        ]

        imageData.getHierarchy().getAnnotationObjects().each { ann ->
            def name = ann.getPathClass()?.getName()
            if (name != null && targetClasses.contains(name)) {
                counts[name]++
            }
        }

        writer.writeLine("\"${imageName}\",Lymphoid Aggregate,${counts['Lymphoid Aggregate']}")

        imageData.close()
        println "Processed: ${imageName}"
    }
}

println "Done: " + outFile.getAbsolutePath()