//This file is bt org.cs533.newprocessor.assembler.Assembler.getFullImage(Assembler.java:95)eing used to test a producer consumer scenario
// using a single lock queue

.data
    // this is the lock variable. It is locked with value 1 and unlocked with value 0
    $lock 0
   $firstMatrixRows 10
   $firstMatrixColumns 10
   $secondMatrixRows 10
   $secondMatrixColumns 10
   $resultMatrixRows 10
   $resultMatrixColumns 10
   
   $currRow 0
   $currCol 0

    $firstMatrix[100]
    //10x15 matrix
    $secondMatrix[100]
    //result must be  a 10x15 matrix as well (r1xc2)
    $resultMatrix[100]
.instructions
    #registerInitialization


	// LOAD VALUES OF MATRIX DIMENSIONS
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
jal #registerInitialization
addi r0 r30 0x4
    #rowLoop
    //here we are going to load the next currRow into r21 and load the next curCol into r22
    //lets set r13 to  be the counter (set it to zero)
    beq r0 r0 #getNextIndex
    #afterResult
    add r0 r0 r13
    //r14 will be the accumulator for the multiply (set it to zero)
    add r0 r0 r14
        #innerRowLoop
            jal #putSource1ValueIntoR29
            jal #putSource2ValueIntoR28
            //multiply the row * column store result in r28
            mul r28 r29 r28
            //accumultate the result from the multiply into r14
            add r28 r14 r14
            //increment counter
            addi r13 r13 0x1
            beq r13 r2 #writeValueToDestination
            beq r13 r13 #innerRowLoop

        #writeValueToDestination
            //write the value back
             jal #writeDestinationToMatrix
             beq r0 r0 #rowLoop


    #lock
       //load r8 with the value 1
        load r8 0x1
        //compare M[$lock] to 0 if it is 0 then swap in the value of 1
        cas r1 r0 r8
        //If r8 and r0 are not equal then continue spinning
        beq r8 r0 #jumpBack
        beq r8 r8 #lock
    #jumpBack
        ret
    #unlock
        //set M[$lock] to 0
        sw r1 r0
        ret

#getNextIndex
    jal #lock
    load r22 $currCol
    //r23 has M[$currCol]
    lw r22 r23
    load r20 $currRow
    // r21 has M[$currRow]
    lw r20 r21
    //ALSO PUT THIS INTO r26
    add r21 r0 r26
    //increment currCol store into r25
    addi r23 r25 0x1
    //subtract the incremented value from the numberofColumns in the second matrix
    sub r25 r5 r27
    bgez r27 #checkRows
    beq r26 r26 #storeNewValuesAndReturn

    #checkRows
        //increment the rows and store in r26
        addi r21 r26 0x1
        //compare if numRows > totalRows
        sub r26 r2 r27
        bgez r27 #unlockAndDone
        // otherwise set Columns to zero
        add r0 r0 r25
    #storeNewValuesAndReturn
        sw r22 r25
        sw r20 r26
        jal #unlock
        beq r0 r0 #afterResult
        
    

#unlockAndDone
    jal #unlock
    halt

#putSource1ValueIntoR29
            mul r21 r3 r29
            //add r29 to counter and store in r29
            add r29 r13 r29
            // multiply this by four to get the byte offset
            mul r29 r30 r29
            //load the word into r29 this is the value of the row
            lw r29 r29
            ret

#putSource2ValueIntoR28
            mul r13 r5 28
            //add r29 to counter and store in r28
            add r28 r23 r28
            // multiply this by four to get the byte offset
            mul r28 r30 r28
            //load the word into r28 this is the value of the row
            lw r28 r28
            ret

#writeDestinationToMatrix
    //curRow * destColumns in r28
    mul r20 r7 r28
    //(currRow*destColumns) + currCol in r28
    add r28 r23 r28
    //get ByteOffset
    mul r28 r30 r28
    //store the word back
    sw r28 r14
    ret
.startpc
#startMultiply
#startMultiply
#startMultiply
#startMultiply
#startMultiply
