################################################################################
#  makefile for jpam
#  author: Greg Luck, David Lutterkort, Seth Theriault
#  Modifications by Francis Galiegue <fgaliegue@gmail.com>

CPPFLAGS = -Wall -Wstrict-prototypes -Wmissing-prototypes -Werror
SONAME = pam4j

clean:
	rm -f *.class *.so *.o

# Linux x86
libjpam.x86: PamHandle.c
	gcc -fPIC $(CPPFLAGS) -g -c -I$(JAVA_HOME)/include \
	    -I$(JAVA_HOME)/include/linux PamHandle.c
	gcc -fPIC $(CPPFLAGS) -g -c -I$(JAVA_HOME)/include \
	    -I$(JAVA_HOME)/include/linux log.c
	gcc -g -shared -fPIC -o lib$(SONAME).so PamHandle.o log.o \
	    -lpam -lpam_misc -ldl

# Linux x86_64
libjpam.x86_64: PamHandle.c
	gcc -fPIC $(CPPFLAGS) -g -c -I$(JAVA_HOME)/include \
	    -I$(JAVA_HOME)/include/linux PamHandle.c
	gcc -fPIC $(CPPFLAGS) -g -c -I$(JAVA_HOME)/include \
	    -I$(JAVA_HOME)/include/linux log.c
	gcc -g -shared -fPIC -o lib$(SONAME).so PamHandle.o log.o \
	    -lpam -lpam_misc -ldl
