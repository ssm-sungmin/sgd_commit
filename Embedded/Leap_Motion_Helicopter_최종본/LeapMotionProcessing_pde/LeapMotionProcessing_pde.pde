import processing.serial.*;
import com.onformative.leap.LeapMotionP5;
import com.leapmotion.leap.Hand;

/************
1s = 20EA
0.05s = 1EA

1 = 50ms
*************/

Serial myPort;
LeapMotionP5 leap;

float xhand;
float yhand;
float zhand;

public class Node {
    int  Throttle, Pitch, Yaw;
    private Node nextNode = null;
 
    public Node(int t, int p, int y){
     Throttle = t;
     Pitch = p;
     Yaw = y;
    }
    
    public Node getData() {
        return new Node(Throttle,Pitch,Yaw);
    }
 
    public Node getNextNode() {
        return nextNode;
    }
 
    public void setNextNode(Node nextNode) {
        this.nextNode = nextNode;
    }
}

public class LinkedQueue {
    private Node front;
    private Node rear;
    private int count=0;
    private int avgThrottle=0;
    private int avgPitch=0;
    private int avgYaw=0;

    public void enqueue(Node newNode) {

        if (front == null) {
            front = newNode;
            rear = newNode;    
        } else {
            rear.setNextNode(newNode);
            rear = newNode;
        }
        
        if(count<MAX)
              count++;
        
        avgThrottle += newNode.Throttle;
        avgPitch += newNode.Pitch;        
        avgYaw += newNode.Yaw;
    }
 
    public void dequeue() {
        Node front = this.front;
 
        avgThrottle -= front.Throttle;
        avgPitch -= front.Pitch;        
        avgYaw -= front.Yaw;
        
        if (front.getNextNode() == null) {
            this.front = null;
            rear = null;
        } else {
            this.front = front.getNextNode();
        }
    }

    public boolean isEmpty() {
        return (front == null);
    }

    public int getSize() {
        return count;
    }
    
    public Node getFront() {
        return front;
    }
    
    public Node getRear() {
        return rear;
    }
    
    public int AvgThrottle(){
      return avgThrottle/count;
    }
    
    public int AvgPitch(){
      return avgPitch/count;
    }
    
    public int AvgYaw(){
      return avgYaw/count;
    }
}

final int MAX = 7;
LinkedQueue valueQueue = new LinkedQueue();

void setup()
{
  frameRate(20);
  size(700, 700, P2D);
  
  //change the 0 to a 1 or 2 etc. to match your port
  String portName = Serial.list()[0];
  
  myPort = new Serial(this, portName, 57600);
  leap = new LeapMotionP5(this);
}

void draw()
{

/* color (R G B) */
  background(200, 50, 0);
  fill(255, 255, 0);
  stroke(30,255,30);
  
/* circular center = ((x+30), (z+320)) >> right bottom (55,55) */
  ellipse(getHandX()+30, getHandZ()+320, 55, 55);

/* InLine((x, y) >> (x',y')) */
  line( 300, 650, 300, 50);
  line( 400, 650, 400, 50);
  line( 50, 300, 650, 300);
  line( 50, 400, 650, 400);
  
/* Block OutLine */
  line(50, 50, 650, 50);
  line(50, 650, 650, 650);
  line(50, 50, 50, 650);
  line(650, 50, 650, 650);


  int handCt = 0;

  for (Hand hand : leap.getHandList()) {
    
    if (handCt == 0)
    {
      PVector handPos = leap.getPosition(hand);
      setHandPos( handPos.x, handPos.y, handPos.z );
    }
    handCt++;
    
    int throttle = (int)map(getHandY(), height-80, +200, 0, 85);
    throttle = constrain(throttle, 0, 85);
    
    int pitch = (int)map(getHandZ(), -height+330, height-330, 171, 255);
    pitch = constrain(pitch, 171, 255);
    
    int yaw= (int)map(getHandX(), width-50, -width+50, 86, 170);
    yaw = constrain(yaw, 86, 170);
    
    if(valueQueue.getSize() >= MAX)
      valueQueue.dequeue();
    valueQueue.enqueue(new Node(throttle,pitch,yaw));       
    
    myPort.write(valueQueue.AvgYaw()); // left, right
    myPort.write(valueQueue.AvgPitch()); // go, back
    myPort.write(valueQueue.AvgThrottle()); //up, down
        
    //println(" throttle " + throttle + " yaw " + yaw + " pitch " + pitch);
    //println(" throttle " + throttle + " yaw " + -(yaw-107) + " pitch " + -(pitch-216));
    //println(" throttle " + valueQueue.AvgThrottle() + " yaw " + valueQueue.AvgYaw() + " pitch " + valueQueue.AvgPitch());
       
    /*
    if (myPort.available() > 0 ) {
         int value = myPort.read();
         println(" send " + yaw + " receive " + value);
    }
    */
    

  }
  
}

void setHandPos(float x, float y, float z)
{
  xhand = x;
  yhand = y;
  zhand = z;
}

float getHandX()
{
  return xhand;
}

float getHandY()
{
  return yhand;
}
float getHandZ()
{
  return zhand;
}