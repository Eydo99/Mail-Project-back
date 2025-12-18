; Data import
sourcedata:
    DB "HELLO WORLD"
    DB "I AM THE IP"

searchdata:
	DB "AAAABBBB"
	DB "BBBBCCCC"

comparedata:
    DB "HELLO WORLD"
    DB "JUKIYYYY"
	
destdata:	
	DB [16,0]
	
start:

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;	

; TestStringCompare:	;Search for a Byte/Word with CMPS

; 		mov cx,10		;Byte/Word Count
; 		mov si, OFFSET sourcedata	;String Source Offset
; 		mov di, OFFSET comparedata ;String Destination Offset		
	
; 		cld 			;Normal direction +1 after each REP
; 		;std			;Reverse Direction -1 after each REP
		
; 		repz cmps byte		;Repeatedly COMPare bytes
; 		;cmpsb			;Do one compare - CX unchanged

	
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;	
	
; TestStringLoad:			;Load a Byte or Word from ram (and inc) with LODS
; 		mov si, OFFSET sourcedata;String Source Offset
		
; 		mov ax,0		;We'll read into AX/AL
		
;  		cld 			;Normal direction +1 after each REP
	
; 		;lodsw
; 		;lodsb
; 		lods byte 		;Load a byte
		
; 		lods byte		;Load a byte
		
; 		lods byte 		;Load a byte
		
; 		lods word		;Load a Word
		
; 		lods word		;Load a Word
		
; 		lods word		;Load a Word
		
; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;	
	
; TestStringScan:			;Scan string for a byte/word 
; 		mov cx,8        ;Scan length (6=fail 8=OK)
	
; 		mov di, OFFSET searchdata ;String Destination Offset
; 		mov ax, di
; 		add ax, 8
; 		mov di, ax
	
; 		; scasb
; 		; scasw
	
; ;  		cld 			;Normal direction +1 after each REP
; ; 		mov ax, 0x0043
; ; 		repnz scas byte	;NZ=Scan for AL ... repeat until found or CX=0 (AKA REPNE)
		
; ; 		std			;Reverse Direction -1 after each REP
; ; 		mov ax, 0x0043
; ; 		repz scas byte		;Z=Scan until not AX ... repeat until found or CX=0 (AKA REPE)

; ;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;	
	
; TestStringStore:	;Save strings with MOVS ,store a string of one byte/words with STOS
; 		mov si,OFFSET sourcedata	;String Source Offset
; 		mov ax, si
; 		add ax, 8
; 		mov si, ax
	
; 		mov di,OFFSET destdata ;String Destination Offset	
; 		mov ax, di
; 		add ax, 8
; 		mov di, ax
			
; 		mov cx,3		;Byte/Word Count

	
; 		cld 			;Normal direction +1 after each REP
; 		;std			;Reverse Direction -1 after each REP
		
; 		;movsw
; 		;movsb
; ; 		rep movs word		;REPeatedly MOVe String cx Words from DS:SI to ES:DI 
; ; 		rep movs byte		;REPeatedly MOVe String cx Bytes
		
; 		mov ax, 0x1122
; 		; stosw
; 		; stosb
; 		rep stos word		;REPeatedly STOre cx times ax to ES:DI 
; 		rep stos byte	;REPeatedly STOre cx times al to ES:DI 

	
	
	
