; SPDX-License-Identifier: MIT

section	.text
   global _start     ;must be declared for linker (ld)
	
_start:	            ;tells linker entry point
   mov	edx,len     
   mov	ecx,msg     
   mov	ebx,1       
   mov	eax,4   
   ; NOSECHUB    
   int	0x80   
   ; END-NOSECHUB     
	
   mov	eax,1       
   int	0x80 

section	.data
msg db 'Hello, world!', 0xa
len equ $ - msg     ;length of the string
