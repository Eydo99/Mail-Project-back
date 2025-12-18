org 100h

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;	

; Multiplication:	;Cases for Byte/Word Multiplication


; 8x8
  mov AX, 0x000F    ; 15
  mov BX, 0x0008    ; 8
  mul BL            ; 15 * 8 = 120 => 0x78

; 16x8
  mov AX, 0x012F    ; 303
  mov BX, 0x000C    ; 13
  mul BL            ; 303 * 13 = 3636 => 0x0E34

; 16x16
  mov AX, 0x21CD    ; 8653
  mov BX, 0x34FB    ; 13563
  mul BX            ; 8653 * 13563 = 117360639 => 0x6FEC7FF
                        
; Signed vs Unsigned
  mov AX, 0x0070    ; 112
  mov BX, 0x00C0    ; 192 ==> -40h ==> -64
  
  ; mul BX          ; usigned: 112 * 192 = 21504 => 0x5400
  imul BL           ; signed:  112 * -64 = -7168 => -0x1C00 => 0xE400
    
; Unpacked BCD
  mov AX, 0x0007    ; BCD 7
  mov BX, 0x0009    ; BCD 9
  mul BX            ; 7 * 9 =  63 => 0x003F
  aam               ; AX => 0x0603   
;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;	

END