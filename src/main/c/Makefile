################################################################################
#  makefile for jpam
#  author: Greg Luck, David Lutterkort, Seth Theriault
#  Modifications by Francis Galiegue <fgaliegue@gmail.com>

CPPFLAGS = -Wall -Wstrict-prototypes -Wmissing-prototypes -Werror

clean:
	rm -f *.class *.so *.o

# Linux x86
libjpam.x86: Pam.c
	gcc -fPIC $(CPPFLAGS) -g -c -I$(JAVA_HOME)/include \
	    -I$(JAVA_HOME)/include/linux \
	    Pam.c PamHandle.c
	gcc -g -shared -fPIC -o libjpam.so Pam.o PamHandle.o -lpam -lpam_misc -ldl

# Linux x86_64
libjpam.x86_64: Pam.c
	gcc -fPIC $(CPPFLAGS) -g -c -I$(JAVA_HOME)/include \
	    -I$(JAVA_HOME)/include/linux \
	    Pam.c PamHandle.c
	gcc -g -shared -fPIC -o libjpam.so Pam.o PamHandle.o -lpam -lpam_misc -ldl
