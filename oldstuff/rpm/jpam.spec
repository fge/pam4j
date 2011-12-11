#
#
# $Log: jpam.spec,v $
# Revision 1.3  2005/06/18 19:18:37  gregluck
# Reverted native class loading patch and added JAAS confiurable service name patch
#
# Revision 1.2  2005/05/19 22:32:36  gregluck
# Fixes for NLD
#
# Revision 1.1  2005/05/09 00:32:26  gregluck
# Start of an ant target for rpm
#
# todo: Missing many packages required to build
# need $JAVA_HOME defined

#%define jpackage_run_jars antlr jakarta-commons-beanutils jakarta-commons-collections jakarta-commons-logging regexp
%define jpackage_run_jars
#%define jpackage_build_jars checkstyle junit ant ant-nodeps 
%define jpackage_build_jars  
#%define jpackage_jars %jpackage_run_jars %jpackage_build_jars
%define jpackage_jars %jpackage_run_jars %jpackage_build_jars

Summary: A JNI Wrapper for the Unix pam(8) subsystem and a JAAS bridge
Name: jpam
Version: 0.5
Release: 1
License: Apache Software License, v. 1.1
Group: Application/Development
URL: http://jpam.sourceforge.net/
Source0: %{name}-%{version}-src.zip
BuildRoot: %{_tmppath}/%{name}-%{version}-%{release}-buildroot

Requires: %jpackage_run_jars
BuildRequires: %jpackage_jars
BuildRequires: gcc make
BuildRequires: pam-devel

%description 
JPam provides a class to access the Unix pam(8) subsystem from
Java, and wraps it in a JAAS LoginModule

%package javadoc
Summary:       Javadoc for %{name}
Group:         Development/Documentation

%description javadoc
Javadoc for %{name}.

%prep
%setup -q

rm -Rfv tools/*.jar
build-jar-repository -p tools/ %jpackage_jars

%build
if [ ! -f "$JAVA_HOME/include/jni.h" ] ; then
    echo "Could not find jni.h. Did you set JAVA_HOME properly ?"
    exit 1
fi
ant shared-object dist-jar javadoc

%install
rm -rf $RPM_BUILD_ROOT

# jar
install -d -m 755 $RPM_BUILD_ROOT%{_javadir}
install -m 644 build/%{name}-%{version}.jar $RPM_BUILD_ROOT%{_javadir}/%{name}-%{version}.jar
(cd $RPM_BUILD_ROOT%{_javadir} && for jar in *-%{version}*; do ln -sf ${jar} `echo $jar| sed  "s|-%{version}||g"`; done)

install -d -m 755 $RPM_BUILD_ROOT%{_docdir}/%{name}-%{version}
install -m 644 src/dist/* $RPM_BUILD_ROOT%{_docdir}/%{name}-%{version}
# FIXME: Sun's JDK does not search for libraries in /usr/lib, though
# IBM's does. This specfile will work for users with the IBM JRE, but not
# for users of Sun's JRE. Unfortunately, the two JRE's don't have a single
# directory in java.library.path in common.
install -D -m 755 build/gen-src/c/libjpam.so $RPM_BUILD_ROOT/usr/lib/libjpam.so

# javadoc
install -d -m 755 $RPM_BUILD_ROOT%{_javadocdir}/%{name}-%{version}
cp -pr site/documentation/javadoc/* $RPM_BUILD_ROOT%{_javadocdir}/%{name}-%{version}

%clean
rm -rf $RPM_BUILD_ROOT

%post javadoc
rm -f %{_javadocdir}/%{name}
ln -s %{name}-%{version} %{_javadocdir}/%{name}

%postun javadoc
if [ "$1" = "0" ]; then
    rm -f %{_javadocdir}/%{name}
fi

%files
%defattr(-,root,root,-)
%{_javadir}/*
/usr/lib/libjpam.so

%doc
%{_docdir}/*

%files javadoc
%defattr(0644,root,root,0755)
%{_javadocdir}/%{name}-%{version}

%changelog
* Mon Apr 18 2005  <dlutter@redhat.com> 0.4-1
- Simplified packaging for version 0.4

* Mon Apr  11 2005  <jesusr@redhat.com> 
- Changed the jvm location to /usr/lib/jvm/java-ibm/ since
  using %{_libdir} causes libjpam to be placed in /usr/lib64
  which doesn't work with the ibm java

* Fri Apr  1 2005  <dlutter@redhat.com> 
- Initial build.


