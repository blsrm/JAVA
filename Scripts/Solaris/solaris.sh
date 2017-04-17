#!/bin/sh

# Simple script to obtain host info from Solaris systems
# Script is divided into sections to match discovery methods

os=`uname -s`
if [ "$os" != "SunOS" ]; then
    echo This script must be run on Solaris
    exit 1
fi

# Set PATH
PATH=/bin:/usr/bin:/sbin:/usr/sbin:/usr/local/sbin:/usr/local/bin
export PATH

# Initialisation
tw_locale=`locale -a | grep -i en_us | grep -i "utf.*8" | head -n 1 2>/dev/null`

LANGUAGE=""
if [ "$tw_locale" != "" ]; then
    LANG=$tw_locale
    LC_ALL=$tw_locale
else
    LANG=C
    LC_ALL=C
fi
export LANG LC_ALL


# Stop alias commands changing behaviour.
unalias -a

# Insulate against systems with -u set by default.
set +u

if [ -w /tmp ] 
then
    # use a /tmp file to capture stderr
    TW_CAPTURE_FILE=/tmp/tideway_status_$$
    export TW_CAPTURE_FILE
    rm -f $TW_CAPTURE_FILE

    tw_capture(){
        TW_NAME=$1
        shift
        echo begin cmd_status_err_$TW_NAME >>$TW_CAPTURE_FILE
        "$@" 2>>$TW_CAPTURE_FILE
        RETURN_VAL=$?
        echo end cmd_status_err_$TW_NAME >>$TW_CAPTURE_FILE

        echo cmd_status_$TW_NAME=$RETURN_VAL >>$TW_CAPTURE_FILE
        return $RETURN_VAL
    }

    tw_report(){
        if [ -f $TW_CAPTURE_FILE ]
        then 
            cat $TW_CAPTURE_FILE 2>/dev/null
            rm -f $TW_CAPTURE_FILE 2>/dev/null
        fi
    }
else
    # can't write to /tmp - do not capture anything
    tw_capture(){
        shift
        "$@" 2>/dev/null
    }

    tw_report(){
        echo "cmd_status_err_status_unavailable=Unable to write to /tmp"
    }
fi 

# replace the following PRIV_XXX functions with one that has the path to a
# program to run the commands as super user, e.g. sudo. For example
# PRIV_LSOF() {
#   /usr/bin/sudo "$@"
# }

# lsof requires superuser privileges to display information on processes
# other than those running as the current user
PRIV_LSOF() {
 sudo "$@"
}

# This function supports running privileged commands from patterns
PRIV_RUNCMD() {
 sudo "$@"
}

# This function supports privileged cat of files.
# Used in patterns and to get file content.
PRIV_CAT() {
 cat "$@"
}

# This function supports privilege testing of attributes of files.
# Used in conjunction with PRIV_CAT and PRIV_LS
PRIV_TEST() {
  test "$@"
}

# This function supports privilege listing of files and directories
# Used in conjunction with PRIV_TEST 
PRIV_LS() {
  ls "$@"
}

# This function supports privilege listing of file systems and related
# size and usage.
PRIV_DF() {
  "$@"
}

tw_which() {
  SAVE=$IFS
  IFS=:
  for d in $PATH
  do
    if [ -x $d/$1 -a ! -d $d/$1 ]
    then
        echo $d/$1
        break
    fi
  done
  IFS=$SAVE
}

# dmidecode requires superuser privileges to read data from the system BIOS
# on Solaris X86 platforms only
PRIV_DMIDECODE() {
    "$@"
}

# ifconfig requires superuser privileges to display the MAC address of each
# interface
PRIV_IFCONFIG() {
    "$@"
}

# dladm requires superuser privileges to display speed, duplex settings, and
# port aggregation information
PRIV_DLADM() {
    "$@"
}

# ndd requires superuser privileges to display any interface speed
# and negotiation settings
PRIV_NDD() {
    "$@"
}

# By default, the standard ps command on Solaris will truncate command lines
# to 80 characters. This affects Solaris 11, Solaris 10 and Solaris 8 & 9
# with certain patches.
#
# In order to display unlimited command lines, there are several options:
#
#   pargs - This tool is available in more recent updates of Solaris 9 and
#           all updates of Solaris 10 and later. This tool requires the
#           proc_owner privilege to display unlimited command lines for all
#           processes.
#
#   /usr/bin/ps - On Solaris 11, the standard ps command can display
#           unlimited command lines by using BSD style command line arguments.
#           This still requires the the tool is run with proc_owner privilege
#
#  /usr/ucb/ps - This tool is part of the UCB compatibility package which is
#           usually installed by default on versions up to and including
#           Solaris 10 (SUNWscpu package). This tool requires the
#           proc_owner privilege to display unlimited command lines for all
#           processes.
#
#           On Solaris 11, the compatibility/ucb is not usually installed
#           by default and in any case, the /usr/ucb/ps command is simply
#           a link to /usr/bin/ps
#
# In order for the Discovery Condition pattern to correctly detect whether
# ps is being executed with proc_owner privilege, this function must accept
# both the ps command and the ppriv command used by pattern.
PRIV_PS() {
    "$@"
}

# See comments above PRIV_PS, above
PRIV_PARGS() {
    "$@"
}

# lputil requires superuser privileges to display any HBA information
PRIV_LPUTIL() {
    "$@"
}

# hbacmd requires superuser privileges to display any HBA information
PRIV_HBACMD() {
    "$@"
}

# emlxadm requires superuser privileges to display any HBA information
PRIV_EMLXADM() {
    "$@"
}

# fcinfo requires superuser privileges to display any HBA information
PRIV_FCINFO() {
    "$@"
}

# pfiles requires superuser privileges to display open port information
# for processes not running as the current user
PRIV_PFILES() {
    "$@"
}

# Formatting directive
echo FORMAT Solaris

# getDeviceInfo
echo --- START device_info

ihn=`hostname 2>/dev/null`
echo 'hostname:' $ihn
if [ -r /etc/resolv.conf ]; then
    echo 'dns_domain:' `awk '/^(search|domain)/ { print $2; exit }' /etc/resolv.conf 2>/dev/null`
fi
echo 'domain:' `domainname 2>/dev/null`
os=""
os_ver=`uname -r | cut -d. -f2`
if [ $os_ver -ge 11 ]; then
    os=`pkg info entire 2>/dev/null | grep "Version:" | awk 'NR > 1 {print $1}' RS='(' FS=')'`
    if [ "$os" = "" ]; then
        os=`pkg info entire | awk '/Branch:/ {print $2}' | nawk -F. '{printf "Oracle Solaris 11.%s.%s.%s.%s", $3, $4, $6, $7;}'`
    fi
fi
if [ "$os" = "" -a -r /etc/release ]; then
    os=`head -1 /etc/release 2>/dev/null`
    update=`grep Update /etc/release 2>/dev/null`
    if [ ! -z "$update" ]; then
        os="$os, $update"
    fi
fi
if [ "$os" = "" ]; then
    os=`uname -sr 2>/dev/null`
fi
echo 'os:' $os
echo 'os_arch:' `isainfo -k 2>/dev/null`

echo --- END device_info

# getHostInfo
echo --- START host_info

os_ver=`uname -r | cut -d. -f2`
if [ $os_ver -ge 11 ]; then
     pkg list -H --no-refresh system/kernel | awk '{print "kernel:", $2; exit}'
else
    showrev 2>/dev/null | nawk -F: '/^Kernel version/ {gsub("^ *", "", $2); print "kernel:", $2; exit}'
fi
echo 'model:' `uname -i 2>/dev/null`
/usr/sbin/prtconf 2>/dev/null | nawk '/^Memory size:/ {print "ram: " $3 "MB"}'

if [ `uname -p` = i386 ]; then
    if [ -x /usr/sbin/smbios ]; then
        /usr/sbin/smbios -t SMB_TYPE_SYSTEM 2>/dev/null | nawk '{
            if( $1 ~ /Manufacturer:/ ) { sub(".*Manufacturer: *","");  printf( "vendor: %s\n", $0 ); }
            if( $1 ~ /Product:/ ) { sub(".*Product: *",""); printf( "model: %s\n", $0 ); }
            if( $1 ~ /Serial/ && $2 ~ /Number:/ ) { sub(".*Serial Number: *",""); printf( "serial: %s\n", $0 ); }
        }'
    else
        if [ -f /usr/sbin/dmidecode ]; then
            PRIV_DMIDECODE /usr/sbin/dmidecode 2>/dev/null | nawk '/DMI type 1,/,/^Handle 0x0*[2-9]+0*/ {
                if( $1 ~ /Manufacturer:/ ) { sub(".*Manufacturer: *","");  printf( "vendor: %s\n", $0 ); }
                if( $1 ~ /Product/ && $2 ~ /Name:/ ) { sub(".*Product Name: *",""); printf( "model: %s\n", $0 ); }
                if( $1 ~ /Serial/ && $2 ~ /Number:/ ) { sub(".*Serial Number: *",""); printf( "serial: %s\n", $0 ); }
            }'
        fi
    fi
else
    # Solaris SPARC - use prtdiag if possible
    run_prtdiag=1
    if [ -x /usr/bin/zonename ]; then
        # Solaris 10 or later, check this is the global zone before attempting
        # to run prtdiag
        if [ `/usr/bin/zonename 2>/dev/null` != "global" ]; then
            # Non global zone, don't run prtdiag
            run_prtdiag=0
        fi
    fi

    if [ $run_prtdiag -eq 1 ]; then
        if [ -x /usr/platform/`uname -m`/sbin/prtdiag ]; then
            platdir=/usr/platform/`uname -m`/sbin
        elif [ -x /usr/platform/`uname -m`/sbin/sparcv9/prtdiag ]; then
            platdir=/usr/platform/`uname -m`/sbin/sparcv9
        elif [ -x /usr/platform/`uname -i`/sbin/prtdiag ]; then
            platdir=/usr/platform/`uname -i`/sbin
        elif [ -x /usr/sbin/prtdiag ]; then
            platdir=/usr/sbin
        else
            platdir=/sbin
        fi
        if [ -x $platdir/prtdiag ]; then
            echo 'begin prtdiag:'
            $platdir/prtdiag -v 2>/dev/null
            echo ''
            echo 'end prtdiag'
        fi
    fi
fi

# Get serial number. We first try sneep as that knows how to collect the
# serial number on the vast majority of Sun/Fujitsu machines. If that is not
# available we try a few obvious fallbacks including any "Chassis Serial Number"
# from prtdiag
if [ -x /opt/SUNWsneep/bin/sneep ]; then
    serial=`/opt/SUNWsneep/bin/sneep 2>/dev/null`
    if [ "$serial" != "unknown" ]; then
        echo "serial: $serial"
    fi
else
    # Sneep isn't available. Check for Fujitsu serialid command
    if [ -x /opt/FJSVmadm/sbin/serialid ]; then
        /opt/FJSVmadm/sbin/serialid | sed -e 's/serialid/serial/'
    fi
fi

echo 'hostid:' `hostid`
if [ -r /etc/ssphostname ]; then
    # E10K support
    echo 'ssphostname:' `cat /etc/ssphostname`
fi

# Solaris 9 and earlier do not support zones directly.
# There isn't any commands to run inside a branded zone to detect if it's a zone.
# However, the zonename is usually listed in /etc/mnttab (for all solaris zones)
# For example:
# Solaris 11: mnttab /zones/sol11z1/root/etc/mnttab mntfs nodevices,zone=sol11z1,sharezone=1,dev=8600002 1396255682
# Solaris 9:  mnttab  /etc/mnttab     mntfs   nodevices,zone=phx-eai-ris-prod1-int,dev=51c0017        1393042650
if [ $os_ver -lt 10 -a -r /etc/mnttab ]; then
    mnttabstr=`cat /etc/mnttab | grep mnttab | grep zone=`
    if [ "$mnttabstr" != "" ]; then
        options=`echo $mnttabstr | awk '{ print $4 }' | tr "," "\n"`
        for member in $options; do
            case $member in
                zone* ) echo $member | sed -e 's/zone=/zonename: /';;
            esac
        done
    fi
fi

if [ -x /usr/bin/zonename ]; then
    # Solaris 10 zone support
    echo 'zonename:' `/usr/bin/zonename 2>/dev/null`
fi
if [ -x /usr/sbin/zoneadm ]; then
    # Solaris 10 zone support
    echo 'begin zoneadm:'
    /usr/sbin/zoneadm list -ip 2>/dev/null
    echo 'end zoneadm:'
fi
if [ -x /usr/sbin/virtinfo ]; then
    # LDOM support for Solaris 11 in system/core-os, and Solaris 9/10 in SUNWcsu
    echo 'begin virtinfo:'
    /usr/sbin/virtinfo -ap 2>/dev/null
    echo 'end virtinfo:'
fi
echo 'begin solaris_uptime_string:'
uptime 2>/dev/null
echo 'end solaris_uptime_string:'

echo --- END host_info

# getMACAddresses
echo --- START netstat_mac

netstat -n -f inet -p 2>/dev/null | awk '{ if ( $4 ~ /SP/ ) { print $NF } }' | sort -u
netstat -n -f inet6 -p 2>/dev/null | awk '{ if ( $3 ~ /local/ ) { print $2 } }' | sort -u

echo --- END netstat_mac

echo --- START ifconfig_mac
PRIV_IFCONFIG ifconfig -a 2>/dev/null

echo --- END ifconfig_mac

# getIPAddresses
echo --- START ifconfig_ip
ifconfig -a 2>/dev/null

echo --- END ifconfig_ip

# getNetworkInterfaces
echo --- START interface_commands
IFCONFIG=`PRIV_IFCONFIG ifconfig -a 2>/dev/null`
OS_VERSION=`uname -r | cut -f2 -d.`
LISTOFNICS=`echo "$IFCONFIG" | awk '/^[^ \t]/ {if ($1!~/lo/) print $1 "\n"}' | sort -u`
LINKNAMES=`echo "$LISTOFNICS" |  sed 's/:[0-9]*://g
s/://g' | sort -u`
echo 'begin ifconfig:'
echo "$IFCONFIG"
echo 'end ifconfig:'
echo 'begin netstat_mac:'
netstat -n -f inet -p 2>/dev/null | awk '{ if ( $4 ~ /SP/ ) { print $1, $NF } }' | sort -u
netstat -n -f inet6 -p 2>/dev/null | awk '{ if ( $3 ~ /local/ ) { print $1, $2 } }' | sort -u
echo 'end netstat_mac:'
DLADM=`tw_which dladm`
GOT_SPEED_DUPLEX=0 # this flag indicates if netstat or ndd -set are required
GOT_NEGOTIATION=0  # this flag indicates if kstat is required
if [ -f "$DLADM" ]; then
    echo 'begin dladm:'
    if [ $OS_VERSION -lt 11 ]; then
        echo 'begin show-dev:'
        PRIV_DLADM $DLADM show-dev 2>/dev/null
        if [ $? -eq 0 ]; then
            GOT_SPEED_DUPLEX=1
        fi
        echo 'end show-dev:'
    else
        echo 'begin show-ether:'
        PRIV_DLADM $DLADM show-ether 2>/dev/null
        if [ $? -eq 0 ]; then
            GOT_NEGOTIATION=1
            GOT_SPEED_DUPLEX=1
        fi
        echo 'end show-ether:'
        echo 'begin show-vlan:'
        PRIV_DLADM $DLADM show-vlan 2>/dev/null
        echo 'end show-vlan:'
    fi
    echo 'begin show-aggr:'
    PRIV_DLADM $DLADM show-aggr 2>/dev/null
    echo 'end show-aggr:'
    echo 'end dladm:'
fi
KSTAT=`tw_which kstat`
if [ -x "$KSTAT" -a $GOT_NEGOTIATION -eq 0 ]; then
    echo 'begin kstats:'
    for NAME in $LINKNAMES; do
        GOT_SPEED_DUPLEX=1
        $KSTAT -p -n $NAME 2>/dev/null
        if [ $? -ne 0 ]; then
            GOT_SPEED_DUPLEX=0
        fi
    done
    echo 'end kstats:'
fi
if [ $OS_VERSION -lt 10 -a $GOT_SPEED_DUPLEX -eq 0 ]; then
    echo 'begin netstats:'
    GOT_SPEED_DUPLEX=1
    for NAME in $LINKNAMES; do
        netstat -k $NAME 2>/dev/null # -k option is not available for Solaris 10 or later
        if [ $? -ne 0 ]; then
            GOT_SPEED_DUPLEX=0
        fi
    done
    echo 'end netstats:'
fi
NDD=`tw_which ndd`
if [ -f "$NDD" -a $OS_VERSION -lt 11 ]; then
    echo 'begin ndd:'
    if [ $GOT_SPEED_DUPLEX -eq 1 ]; then # ndd -get may provide negotiation info
        for NAME in $LINKNAMES; do
            NIC_TYPE=`echo $NAME | sed 's/[0-9.]*//g'`
            case $NIC_TYPE in
                dmfe | bge | nxge | igb)
                    echo 'NDD :' $NAME ':adv_autoneg_cap:' `PRIV_NDD $NDD -get /dev/$NAME adv_autoneg_cap 2>/dev/null`
                ;;
                *)  # ce, ipge (kstat) / ge, hme, qfe, eri, fjqe, fjgi (skipped to avoid ndd -set) / unknown interfaces
                    continue
                ;;
            esac
        done
    else # ndd -set / -get to retrieve speed, duplex, and negotiation: only if all the other commands failed
        VARS="link_status link_speed ifspeed link_mode link_duplex duplex adv_cap_autoneg adv_autoneg_cap adv_1000autoneg_cap"
        vars_hme="link_speed link_mode adv_autoneg_cap"
        vars_eri="adv_autoneg_cap"
        vars_bge="adv_autoneg_cap link_duplex link_speed"
        vars_dmfe="link_speed link_mode adv_autoneg_cap"
        vars_qfe="link_mode adv_autoneg_cap"
        vars_ge="link_mode adv_1000autoneg_cap"
        vars_ce="adv_autoneg_cap"
        vars_fjqe="link_mode link_speed adv_autoneg_cap"
        vars_fjgi="link_mode link_speed adv_autoneg_cap"
        vars_igb="link_duplex link_speed adv_autoneg_cap"
        LISTOFTYPES=`echo "$LINKNAMES" | sed 's/[0-9.]*$//g' | sort -u`
        LISTOFSETTYPES=`echo $LISTOFTYPES | sed -e 's/bge//' -e 's/dmfe//' -e 's/igb//' -e s'/vsw//'`
        for iface in $LISTOFSETTYPES; do
            eval initial_$iface=`PRIV_NDD $NDD -get /dev/$iface instance 2>/dev/null`
        done
        for NAME in $LINKNAMES; do
            NIC_TYPE=`echo $NAME | sed 's/[0-9.]*//g'`
            NIC_NUMBER=`echo $NAME | sed 's/[a-z]*//'`
            eval vars=\$vars_$NIC_TYPE
            case $NIC_TYPE in
                ge | hme | ce | qfe | eri | fjqe | fjgi) # interfaces that need the -set option:
                    PRIV_NDD $NDD -set /dev/$NIC_TYPE instance $NIC_NUMBER 2>/dev/null
                    instance=`PRIV_NDD $NDD -get /dev/$NIC_TYPE instance 2>/dev/null`
                    if [ $instance -a $instance != "$NIC_NUMBER" ]; then
                      echo "Skipping $NAME : ndd  -set failed"
                      continue
                    fi
                    for var in $vars
                    do
                      echo 'NDD :' $NAME ':' $var ':' `PRIV_NDD $NDD -get /dev/$NIC_TYPE $var 2>/dev/null`
                    done
                ;;
                bge | dmfe | igb) #interfaces that do not need -set:
                    for var in $vars
                    do
                      echo 'NDD :' $NAME ':' $var ':' `PRIV_NDD $NDD -get /dev/$NAME $var 2>/dev/null`
                    done
                ;;
                dman) # Known but ignored interfaces
                    continue
                ;;
                *) # unknown interface
                    echo unknown interface: $NIC_TYPE
                    continue
                ;;
            esac
        done
        for iface in $LISTOFSETTYPES; do
            eval instance=\$initial_$iface
            PRIV_NDD $NDD -set /dev/$iface instance $instance 2>/dev/null
        done
    fi
    echo 'end ndd:'
fi

echo --- END interface_commands

# getNetworkConnectionList
echo --- START netstat

netstat -an -f inet 2>/dev/null | grep -v '^ *\*\.\*'
netstat -an -f inet6 2>/dev/null | grep -v '^ *\*\.\*'

echo --- END netstat

# getProcessList
echo --- START ps
os_ver=`uname -r | cut -d. -f2`
if [ $os_ver -ge 10 -a  -x /usr/bin/zonename ]; then
    zone=`/usr/bin/zonename 2>/dev/null`
    ps -eo pid,ppid,uid,user,zone,args 2>/dev/null | nawk "\$5~/^($zone|ZONE)$/ {print}"
else
    ps -eo pid,ppid,uid,user,args 2>/dev/null
fi
if [ $os_ver -ge 11 ]; then
    PRIV_PS /usr/bin/ps axww 2>/dev/null
else
    if [ -x /usr/ucb/ps ]; then
        PRIV_PS /usr/ucb/ps -axww 2>/dev/null
    fi
fi
if [ -x /usr/bin/pargs -a -d /proc ]; then
    echo begin pargs:
    PRIV_PARGS /usr/bin/pargs `ls -1 /proc` 2>/dev/null
    echo end pargs:
fi

echo --- END ps

# getPatchList
echo --- START patch_list

os_ver=`uname -r | cut -d. -f2`
if [ $os_ver -lt 11 ]; then
    showrev -p 2>/dev/null | grep -v "No patches are installed" | cut -c-16 | nawk '{print $2;}'
else
    echo NO PATCHES
fi

echo --- END patch_list

# getProcessToConnectionMapping
echo --- START lsof-i
temp_lc=$LC_ALL
LC_ALL=C
export LC_ALL
# if [ `uname -r | cut -d. -f2` -lt 7 ]; then
#    PRIV_LSOF lsof -l -n -P -F ptPTn -C -i 2>/dev/null
#else
#    PRIV_LSOF lsof -l -n -P -F ptPTn -i 2>/dev/null
# fi
PRIV_LSOF lsof -l -n -P -F ptPTn -i 2>/dev/null
LC_ALL=$temp_lc
export LC_ALLl
echo --- END lsof-i

# getPackageList
echo --- START pkginfo
os_ver=`uname -r | cut -d. -f2`
if [ $os_ver -ge 11 ]; then
    echo begin pkg_list:
    echo arch: `uname -p`
    pkg list -H --no-refresh 2>/dev/null
    echo end pkg_list:
fi
PKGINFO=`tw_which pkginfo`
if [ ! -z "$PKGINFO" -a -x $PKGINFO ]; then
    pkginfo -l 2>/dev/null
fi

echo --- END pkginfo

# getHBAList
echo --- START hba_fcinfo
echo begin fcinfo_hba:
PRIV_FCINFO fcinfo hba-port
echo end fcinfo_hba:

echo --- END hba_fcinfo

echo --- START hba_emlxadm
PATH=/opt/EMLXemlxu/bin:$PATH

echo begin emlxadm_get_host_attrs:
PRIV_EMLXADM emlxadm devctl -y get_host_attrs
echo end emlxadm_get_host_attrs:

echo --- END hba_emlxadm

echo --- START hba_hbacmd

PATH=/usr/sbin/hbanyware:$PATH

echo begin hbacmd_listhbas:
PRIV_HBACMD hbacmd ListHBAs
echo end hbacmd_listhbas:

echo begin hbacmd_hbaattr:
for WWPN in `PRIV_HBACMD hbacmd ListHBAs 2>/dev/null | awk '/Port WWN/ {print $4;}'`
do
    PRIV_HBACMD hbacmd HBAAttrib $WWPN 2>/dev/null
done
echo end hbacmd_hbaattr:

echo --- END hba_hbacmd

# getServices
#   ** UNSUPPORTED **

# getFileSystems
echo --- START df
echo begin df:
PRIV_DF df -lk 2>/dev/null
echo end df:
echo begin mount:
if [ -r /etc/mnttab ]; then
    cat /etc/mnttab
fi
echo end mount:
echo begin xtab:
if [ -r /etc/xtab ]; then
    cat /etc/xtab
fi
echo end xtab:
echo begin smbclient:
smbclient -N -L localhost
echo end smbclient:
echo begin smbconf:
configfile=`smbstatus -v 2>/dev/null | grep "using configfile" | awk '{print $4;}'`
if [ "$configfile" != "" ]; then
    if [ -r $configfile ]; then
        cat $configfile
    fi
fi
echo end smbconf:

echo --- END df

