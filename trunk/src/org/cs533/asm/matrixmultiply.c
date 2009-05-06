//This file is bt org.cs533.newprocessor.assembler.Assembler.getFullImage(Assembler.java:95)eing used to test a producer consumer scenario
// using a single lock queue

.data
    // this is the lock variable. It is locked with value 1 and unlocked with value 0
    $lock 0
   $firstMatrixRows 10
   $firstMatrixColumns 10
   $secondMatrixRows 10
   $secondMatrixColumns 15 
   $resultMatrixRows 10
   $resultMatrixColumns 15 
   
   $doneRowNumber 0
   $doneColumnNumber 0

    $firstMatrix[100]
    //10x15 matrix
    $secondMatrix[150]
    //result must be  a 10x15 matrix as well (r1xc2)
    $resultMatrix[150]
.instructions
    #registerInitialization
	// LOAD VALUES OF MATRIX DIMENSIONS
	// 
	//

	//r2 stores the VALUE of number matrix rows in firstMatrix
	loadv r2 $firstMatrixRows
	//r3 stores the value of number of matrix columns in firstMatrix
	loadv r3 $firstMatrixColumns
	//r4 stores the VALUE of number of rows in secondMatrix
	loadv r4 $secondMatrixRows
	//r5 stores the VALUE of the number of columns in the secondMatrix
	loadv r5 $secondMatrixColumns
	//r6 stores the VALUE of the number of rows in the resultMatrix
	loadv r6 $resultMatrixRows
	//r7 stores the VALUE of the number of columns in the resultMatrix
	loadv r7 $resultMatrixColumns

	// LOAD POINTERS TO MATRIX AND LOCKS
	
	//Make r1 stores the lock pointer
        load r1 $lock
	//load r9 with address to firstMatrix head
	load r9 $firstMatrix
	//load r10 with address to secondMatrix head
	load r10 $secondMatrix
	//load r11 with address to result matrix
  	load r11 $resultMatrix 
	//load r12 with ponter to doneRowValue 
	
	//return
	ret
	

   #startMultiply
	

    #lock
        //compare M[$lock] to 0 if it is 0 then swap in the value of 1
        cas r1 r0 r5
        //If r5 and r0 are not equal then continue spinning
        beq r5 r0 #jumpBack
        beq r5 r5 #lock
    #jumpBack
	ret
    #unlock
        //set M[$lock] to 0
        sw r1 r0
	ret
.startpc
#startMultiply
