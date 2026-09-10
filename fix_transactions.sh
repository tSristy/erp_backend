#!/bin/bash
FILES=(
  "/Volumes/Backup/my-team/erp_backend/src/main/java/org/enterprise/inventory/service/StockReclassificationService.java"
  "/Volumes/Backup/my-team/erp_backend/src/main/java/org/enterprise/inventory/service/StockTransferService.java"
  "/Volumes/Backup/my-team/erp_backend/src/main/java/org/enterprise/inventory/service/GoodsReceiptService.java"
)

for file in "${FILES[@]}"; do
  sed -i '' -e 's/        reclass.getDetails().clear();/        reclass.getDetails().removeIf(d -> !processedDetails.contains(d));\n        for (var pd : processedDetails) {\n            if (!reclass.getDetails().contains(pd)) {\n                reclass.getDetails().add(pd);\n            }\n        }/g' "$file"
  
  sed -i '' -e 's/        reclass.getDetails().addAll(processedDetails);//g' "$file"

  sed -i '' -e 's/        transfer.getDetails().clear();/        transfer.getDetails().removeIf(d -> !processedDetails.contains(d));\n        for (var pd : processedDetails) {\n            if (!transfer.getDetails().contains(pd)) {\n                transfer.getDetails().add(pd);\n            }\n        }/g' "$file"
  
  sed -i '' -e 's/        transfer.getDetails().addAll(processedDetails);//g' "$file"

  sed -i '' -e 's/        goodsReceipt.getDetails().clear();/        goodsReceipt.getDetails().removeIf(d -> !processedDetails.contains(d));\n        for (var pd : processedDetails) {\n            if (!goodsReceipt.getDetails().contains(pd)) {\n                goodsReceipt.getDetails().add(pd);\n            }\n        }/g' "$file"
  
  sed -i '' -e 's/        goodsReceipt.getDetails().addAll(processedDetails);//g' "$file"
done
