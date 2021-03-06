#!/usr/bin/bash

# Custom path declaration

HOMEDIR=/home
PASSWD_FILE=/etc/passwd
SUDO_FILE=/usr/local/etc/sudoers
if [ ! -f "$SUDO_FILE" ]; then
   SUDO_FILE=/etc/sudoers
fi

MKDIR_PATH=/usr/bin/mkdir
if [ ! -f "$MKDIR_PATH" ]; then
   MKDIR_PATH=/bin/mkdir
fi

CHOWN_PATH=/usr/bin/chown
if [ ! -f "$CHOWN_PATH" ]; then
   CHOWN_PATH=/bin/chown
fi

CHMOD_PATH=/usr/bin/chmod
if [ ! -f "$CHMOD_PATH" ]; then
   CHMOD_PATH=/bin/chmod
fi


# Variable declation
PATROLUSER=patrol
USERNAME=bmcadmin
NEWUSERID=109
NEWUSERHOME=$HOMEDIR/$USERNAME
PATROLGID=800

### Group exists in server check
  PATROLGID=`id -g $PATROLUSER`

echo  "$PATROLGID=Checking"

if [ "$PATROLGID" != "800" ]; then
  echo "Group Creation"
  groupadd -g 800 "$PATROLUSER"
  PATROLGID=800
fi



### Check UID and Username exists for BMCADMIN and create user accordingly

EXISTS_USERNAME=`grep "$NEWUSERID" $PASSWD_FILE | awk -F: '{print $3,$1}' | grep "^$NEWUSERID" | awk '{print $2}'`

### Now step to create bmcadmin user

if [ "$EXISTS_USERNAME" != "$USERNAME" ]; then

  grep "$NEWUSERID" $PASSWD_FILE | awk -F: '{print $3,$1}' | grep "^$NEWUSERID"

  if [ $? = 0 ]; then
  	/usr/sbin/useradd -g "$PATROLGID" -m -d "$NEWUSERHOME" -s /bin/ksh -c "Agentless Discovery" -k /etc/skel "$USERNAME"
  else
  	/usr/sbin/useradd -u "$NEWUSERID" -g "$PATROLGID" -m -d "$NEWUSERHOME" -s /bin/ksh -c "Agentless Discovery" -k /etc/skel "$USERNAME"
  fi
  
  /usr/lbin/modprpw -m nullpw=YES,mintm=0,lftm=0,exptm=0,expwarn=0 bmcadmin
  /usr/sam/lbin/usermod.sam -p "" bmcadmin
  /usr/bin/passwd -d "$USERNAME"
  $MKDIR_PATH "$NEWUSERHOME"/.ssh
  $CHMOD_PATH 700 "$NEWUSERHOME"/.ssh

  echo 'ssh-rsa AAAAB3NzaC1yc2EAAAABJQAAAIEAmGPd5wuopDLwsQoYAO2BEEzILYPSBr+sxLCI9w+1LfclZZdpr4xCGO5WRaMyBg4U8hHBWiuwr+SJ7qoBCenhwjgSIECPYjVX2qc1HY7F+FZmKcsXtsjiR3srG3sO68GiVko9RZCAWBWL6h0D0i6YBY405ochUVvgflD1dJAsyyM= CDNLinux@o2.com' > "$NEWUSERHOME"/.ssh/authorized_keys
  echo 'ssh-rsa AAAAB3NzaC1yc2EAAAABJQAAAIEAy/yUktfe+4HCnqR5Qle34ERyCOhtplqyyhgnOrM1+ZCPv/vIdt45dWUk2lgD7kYCbGycBMEXaOL3z50jtQunRGD96x9aI7N45rK4NnkHROaL4bRliffDvbDcauleDvIe7KjJxlK5li4TCRHJEvrnmmJFDllJYl0QsBnqG/Hm7CE= rsa-key-20131210' >> "$NEWUSERHOME"/.ssh/authorized_keys
  $CHMOD_PATH 600 "$NEWUSERHOME"/.ssh/authorized_keys
  $CHOWN_PATH "$USERNAME":"$PATROLUSER" "$NEWUSERHOME"/.ssh/authorized_keys
  $CHOWN_PATH "$USERNAME":"$PATROLUSER" "$NEWUSERHOME"/.ssh
  $CHOWN_PATH "$USERNAME":"$PATROLUSER" "$NEWUSERHOME"


fi

### SUDO file modification for bmcadmin
grep -i "User_Alias" $SUDO_FILE  | grep -i "BMC"

if [ $? = 1 ]; then
  echo "Adding BMC entry in Sudoers file - $SUDO_FILE"
  echo '' >> $SUDO_FILE
  echo 'User_Alias BMC=%patrol' >> $SUDO_FILE
  echo 'BMC ALL= ALL ' >> $SUDO_FILE
  echo 'bmcadmin ALL=(root) NOPASSWD: ALL' >> $SUDO_FILE
else
  grep -i "bmcadmin" $SUDO_FILE
  if [ $? = 1 ]; then
    echo "BMC profile Already Exist, Adding entry for bmcadmin in Sudoers file - $SUDO_FILE"
    echo '' >> $SUDO_FILE
    echo 'bmcadmin ALL=(root) NOPASSWD: ALL' >> $SUDO_FILE
  fi

fi


echo "Validation Steps ................................................."
echo
cat /etc/passwd | grep -i bmcadmin
cat /etc/group | grep -i ":800:"
ls -ld /home/bmcadmin
ls -ld /home/bmcadmin/.ssh
ls -l /home/bmcadmin/.ssh/authorized_keys
cat -n /home/bmcadmin/.ssh/authorized_keys
echo
echo
cat /etc/sudoers | grep -i bmc
echo
echo
