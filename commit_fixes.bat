@echo off
cd /d "c:\Milk record"
git add app/src/main/java/com/milkdiary/app/db/PaymentDao.java
git add app/src/main/java/com/milkdiary/app/db/MilkRecordDao.java
git add app/src/main/java/com/milkdiary/app/db/SaleDao.java
git add app/src/main/java/com/milkdiary/app/model/MilkRecord.java
git commit -m "fix: resolve critical cursor resource leaks and improve data handling" -m "- Convert PaymentDao cursor operations to try-with-resources blocks (getAllPayments, getPaymentsByType)" -m "- Convert MilkRecordDao cursor operations to try-with-resources blocks (17 methods)" -m "- Add null checks and !isNull(0) checks for SUM aggregates in SaleDao" -m "- Increase MAX_EDIT_COUNT from 1 to 5 for better UX" -m "- Improve robustness against resource leaks and null pointer exceptions" -m "Co-authored-by: Copilot <223556219+Copilot@users.noreply.github.com>"
echo Commit complete. Now pushing...
git push origin main
