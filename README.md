# qupath-lymphoid-aggregate-analysis
Groovy scripts for automated detection, classification, and quantification of lymphoid aggregates in whole-slide immunofluorescence images using QuPath (v0.6.0).

## Overview

This pipeline performs sequential image processing steps including channel setup, tissue detection, cell detection and classification, and lymphoid aggregate identification.

Lymphoid aggregates are defined based on **lymphocyte populations only**, specifically:
- CD3+ T cells
- CD20+ B cells

---

## Requirements

- QuPath v0.6.0
- Whole-slide immunofluorescence images
- Pre-trained cell classifiers for:
  - CD3+ cells
  - CD20+ cells
---

## Pipeline Scripts

Scripts are designed to be run sequentially:

### Image Preprocessing
- `001_Set_channels.groovy`  
  Sets fluorescence channels for analysis.

- `002_Tissue_detect.groovy`  
  Detects tissue regions to restrict downstream analysis. (Need to make a pixel classifier based on nuclear staining and name it Tissue_Detection prior to running script.)

---


### Cell Detection and Classification
- `003_Cell_detection.groovy`  
  Performs cell segmentation.

- `004_Cell_classify.groovy`  
  Applies trained classifiers to identify CD3+ and CD20+ cells.

---

### Tissue and Aggregate Detection
- `005_Clean_up_Tissue.groovy`  
  Refines tissue annotations and removes artifacts. (Manually select artifacts and classify as *Ignore prior to running.)

- `006_Detect_Aggregates.groovy`  
  Identifies candidate lymphoid aggregates based on local lymphocyte density  
  (CD3+ and CD20+ cells only).

- `007_Merge_Aggregates.groovy`  
  Merges adjacent high-density regions into aggregate structures.

- `008_Clean_up_Aggregates.groovy`  
  Removes small or spurious aggregate detections.

---

### Aggregate Classification
- `009_Classify_Lymphoid Aggregate.groovy`  
  Classifies aggregates based on predefined criteria using:
  - Total lymphocyte density (CD3+ + CD20+)
  - B cell proportion (CD20+ fraction)

  Aggregates are categorized into:
  - Lymphoid Aggregates (B cell–containing aggregates)
  - Unclassified

---

### Quantification and Export
- `010_Batch_Count and export_Lymphoid ...groovy`  
  Batch processes images to:
  - Count classified aggregates
  - Export quantitative data as CSV files

---

## Methods Summary

Whole-slide images are partitioned into tiles, and lymphocyte density is calculated using CD3+ and CD20+ cells. High-density regions are merged to form candidate aggregates, which are subsequently filtered and classified based on size and cellular composition.

Aggregate classification is restricted to **T cells and B cells**, reflecting lymphocyte-driven organization. Plasma cells are excluded from aggregate definition but may be analyzed separately.

---

## Usage

1. Open image in QuPath
2. Run scripts sequentially (001 → 010)
3. Ensure classifiers are loaded before running classification steps
4. Exported data will be saved as CSV files for downstream analysis

---

## Output

- Aggregate counts per image
- Aggregate classifications
- Cell composition metrics

---

## Notes

- Script parameters (e.g., tile size, lymphocyte thresholds) are defined within individual scripts.
- Aggregate definitions align with those described in the manuscript Methods.
