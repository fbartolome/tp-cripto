# Shared Secret in Images with Steganography
Cryptography and Security Project

## Getting Started

These instructions will install the system in your local machine.
 
### Prerequisites
 
1. Install Maven, if you haven't yet

    #### Mac OS X
    ```
    brew install maven
    ```
    
    #### Ubuntu
    ```
    sudo apt-get install maven
    ```
    
    #### Other OSes
    Check https://maven.apache.org/install.html.


2. Clone the repository or download source code:
```
git clone https://github.com/lipusal/tp-cripto.git
```    

### Installing

1. Change working directory to project root (i.e where pom.xml is located):
    ```
    cd <project-root>
    ```

2. Let maven resolve dependencies:
    ```
    mvn dependency:resolve
    ```

3. Create jar file
    ```
    mvn clean compile assembly:single
    ```


## Usage
The application can be executed running ```java -jar <path-to-jar> <program parameters>```.
It can be configured in order to execute in different modes. This is done including parameters. The following sections will describe these modes. 

### Displaying usage message
You can display the usage message by setting the ```-h``` or the ```--help``` parameters.
Example of usage:
```
java -jar <path-to-jar> -h
```


### Indicating action to perform
It can either be to recover a picture from some given shadows or to distribute a secret picture among other shadow pictures

Options are:
* To recover `-r`
* To distribute `-d`


### Indicating output file
If used with -d, the image to distribute.  If  used  with -r, the output file for the recovered image.

```-secret <secretFileName>```

### Indicating k
K is the minimum number of shadows needed to retrieve the picture

```-k K```


### Indicating n
Only allowed when  used  with  -d.  Total  number  of  shadows  to  generate.  If not provided, will make n the number of pictures in the specified directory.

``` -n N ```

### Indicating Directory
If used with -d, directory  containing  images  where  secret will be distributed. If used with -r, directory  containing  images  from  which  to  recover  the secret. In either case, default is current directory. (default: ./)

``` -dir <path> ```


## Authors
* Juan Li Puma
* Martin Goffan
* Natalia Navas
* Francisco Bartolomé