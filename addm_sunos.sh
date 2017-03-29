#!/usr/bin/bash
 
 
# Custom path declaration
HOMEDIR=/export/home
PASSWD_FILE=/etc/passwd
SUDO_FILE=/usr/local/etc/sudoers
if [ ! -f "$SUDO_FILE" ]; then
   SUDO_FILE=/etc/sudoers
fi
 
 
# Variable declation
PATROLUSER=patrol
USERNAME=bmcadmin
NEWUSERID=109
NEWUSERHOME=$HOMEDIR/$USERNAME
PATROLGID=800
PROC_OWNER=`ppriv -l proc_owner`
USRPRIV=`ppriv -v $$ | grep "P:" | awk '{print $2}'`
 
### Check UID and Username exists for BMCADMIN and create user accordingly
 
EXISTS_USERNAME=`grep "$NEWUSERID" $PASSWD_FILE | awk -F: '{print $3,$1}' | grep "^$NEWUSERID" | awk '{print $2}'`
 
if [ "$EXISTS_USERNAME" != "$USERNAME" ]; then
 
#  ### Modifying existing users files permission
#  grep "$NEWUSERID" $PASSWD_FILE | awk -F: '{print $3,$1}' | grep "^$NEWUSERID"
#  if [ $? = 0 ]; then
#     LASTUID=`sort -t : -k 3,3n $PASSWD_FILE |awk -F: '{print $3}' | tail -1`
#     NEWUID=`expr $LASTUID + 1`
#     #/usr/sbin/usermod -u $NEWUID $EXISTS_USERNAME
#     #`/usr/bin/find / -user $NEWUSERID -exec chown $NEWUID {} \;`
#  fi
 
  grep "$NEWUSERID" $PASSWD_FILE | awk -F: '{print $3,$1}' | grep "^$NEWUSERID"

  if [ $? = 0 ]; then
  	/usr/sbin/useradd -g "$PATROLGID" -m -d "$NEWUSERHOME" -s /bin/ksh -c "Agentless Discovery" -k /etc/skel "$USERNAME"
  else
  	/usr/sbin/useradd -u "$NEWUSERID" -g "$PATROLGID" -m -d "$NEWUSERHOME" -s /bin/ksh -c "Agentless Discovery" -k /etc/skel "$USERNAME"
  fi
 
  ### Now step to create bmcadmin user
#  /usr/sbin/useradd -u "$NEWUSERID" -g "$PATROLGID" -m -d "$NEWUSERHOME" -s /bin/ksh -c "Agentless Discovery" -k /etc/skel "$USERNAME"
  /usr/bin/passwd -d "$USERNAME"
  /usr/bin/mkdir "$NEWUSERHOME"/.ssh
  /usr/bin/chmod 700 "$NEWUSERHOME"/.ssh
 
  echo 'ssh-rsa AAAAB3NzaC1yc2EAAAABJQAAAIEAmGPd5wuopDLwsQoYAO2BEEzILYPSBr+sxLCI9w+1LfclZZdpr4xCGO5WRaMyBg4U8hHBWiuwr+SJ7qoBCenhwjgSIECPYjVX2qc1HY7F+FZmKcsXtsjiR3srG3sO68GiVko9RZCAWBWL6h0D0i6YBY405ochUVvgflD1dJAsyyM= CDNLinux@o2.com' > "$NEWUSERHOME"/.ssh/authorized_keys
 
  echo 'ssh-rsa AAAAB3NzaC1yc2EAAAABJQAAAIEAy/yUktfe+4HCnqR5Qle34ERyCOhtplqyyhgnOrM1+ZCPv/vIdt45dWUk2lgD7kYCbGycBMEXaOL3z50jtQunRGD96x9aI7N45rK4NnkHROaL4bRliffDvbDcauleDvIe7KjJxlK5li4TCRHJEvrnmmJFDllJYl0QsBnqG/Hm7CE= rsa-key-20131210' >> "$NEWUSERHOME"/.ssh/authorized_keys
 
  /usr/bin/chmod 600 "$NEWUSERHOME"/.ssh/authorized_keys
  /usr/bin/chown "$USERNAME":"$PATROLUSER" "$NEWUSERHOME"/.ssh/authorized_keys
  /usr/bin/chown "$USERNAME":"$PATROLUSER" "$NEWUSERHOME"/.ssh
  /usr/bin/chown "$USERNAME":"$PATROLUSER" "$NEWUSERHOME"
 
  if [ "$PROC_OWNER" != "" -a "$USRPRIV" != "" ]; then
     /usr/sbin/usermod -K defaultpriv="$USRPRIV","$PROC_OWNER" "$USERNAME"
  fi
 
fi
 
 
### SUOD file modification for bmcadmin
 
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

