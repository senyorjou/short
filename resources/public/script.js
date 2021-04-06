// minimal set of functions

const createShort = async () => {
     const shortUrlVal = document.getElementById("url").value;
     const infoDiv = document.getElementById("short-link");

     let formData = new FormData();
     formData.append("url", shortUrlVal);

     const options = {
          method: "POST",
          body: formData,
     }

     const response = await fetch("http://localhost:3000", options);

     if (response.ok) {
          infoDiv.innerHTML = await response.text();
     } else {
          infoDiv.innerHTML = "Some problems creating the short url";
     }

}


const getShortInfo = async () => {
     const shortUrlVal = document.getElementById("short-url").value;
     const infoDiv = document.getElementById("link-info");

     const response = await fetch(`http://localhost:3000/${shortUrlVal}/+`);

     if (response.ok) {
          infoDiv.innerHTML = await response.text();
     } else {
          infoDiv.innerHTML = "Some problems fetching the value";
     }
}
