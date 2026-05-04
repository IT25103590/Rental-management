<%--
  Created by IntelliJ IDEA.
  User: sajeenth
  Date: 4/14/2026
  Time: 8:10 PM
--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <title>Bike Rental System - Return Bike</title>
    <script src="https://cdn.tailwindcss.com"></script>
    <script src="https://cdnjs.cloudflare.com/ajax/libs/animejs/3.2.1/anime.min.js"></script>
    <style>
        body {
            background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
            min-height: 100vh;
            display: flex;
            align-items: center;
            justify-content: center;
            font-family: 'Inter', sans-serif;
            padding: 2rem;
        }
        .glass {
            background: rgba(255, 255, 255, 0.15);
            backdrop-filter: blur(10px);
            -webkit-backdrop-filter: blur(10px);
            border: 1px solid rgba(255, 255, 255, 0.18);
            border-radius: 1rem;
            box-shadow: 0 8px 32px 0 rgba(31, 38, 135, 0.37);
        }
        .input-glass {
            background: rgba(255, 255, 255, 0.1);
            border: 1px solid rgba(255, 255, 255, 0.2);
            color: white;
        }
        .input-glass::placeholder {
            color: rgba(255, 255, 255, 0.6);
        }
    </style>
</head>
<body class="flex flex-col gap-8">

    <div class="glass p-8 w-full max-w-md animate-content opacity-0">
        <h2 class="text-3xl font-bold text-white mb-6 text-center">Return Bike</h2>

        <%
            String message = (String) request.getAttribute("message");
            if (message != null) {
                String colorClass = message.contains("Error") ? "bg-red-500/30 text-red-100" : "bg-green-500/30 text-green-100";
        %>
            <div class="<%= colorClass %> p-3 rounded-lg mb-6 border border-white/20 text-center text-sm font-medium">
                <%= message %>
            </div>
        <%
            }
        %>

        <form action="ReturnBikeServlet" method="post" class="flex flex-col gap-4">
            <div>
                <label class="block text-white text-sm mb-2" for="rentalId">Rental ID</label>
                <input type="text" name="rentalId" id="rentalId" placeholder="e.g. R001" required
                       class="input-glass w-full p-3 rounded-lg focus:outline-none focus:ring-2 focus:ring-purple-400 transition-all">
            </div>

            <button type="submit" 
                    class="bg-white text-purple-700 font-bold py-3 px-6 rounded-lg hover:bg-purple-100 transform transition-all active:scale-95 mt-4">
                Return Bike
            </button>
        </form>
    </div>

    <div class="glass p-8 w-full max-w-4xl animate-content opacity-0">
        <h3 class="text-2xl font-bold text-white mb-6">Current Rentals</h3>
        <div class="overflow-x-auto">
            <table class="w-full text-left text-white">
                <thead class="border-b border-white/20">
                    <tr>
                        <th class="py-3 px-4">Rental ID</th>
                        <th class="py-3 px-4">User ID</th>
                        <th class="py-3 px-4">Bike ID</th>
                        <th class="py-3 px-4 text-center">Status</th>
                    </tr>
                </thead>
                <tbody>
                    <%
                        String filePath = application.getRealPath("/Data/rentals.txt");
                        java.io.File file = new java.io.File(filePath);
                        if (file.exists()) {
                            try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.FileReader(file))) {
                                String line;
                                boolean isHeader = true;
                                while ((line = br.readLine()) != null) {
                                    if (isHeader) {
                                        isHeader = false;
                                        continue;
                                    }
                                    String[] data = line.split(",");
                                    if (data.length >= 4) {
                                        String statusColor = data[3].equalsIgnoreCase("Returned") ? "bg-green-500/40" : "bg-yellow-500/40";
                    %>
                    <tr class="border-b border-white/10 hover:bg-white/5 transition-colors">
                        <td class="py-3 px-4"><%= data[0] %></td>
                        <td class="py-3 px-4"><%= data[1] %></td>
                        <td class="py-3 px-4"><%= data[2] %></td>
                        <td class="py-3 px-4 text-center">
                            <span class="<%= statusColor %> px-3 py-1 rounded-full text-xs font-semibold">
                                <%= data[3] %>
                            </span>
                        </td>
                    </tr>
                    <%
                                    }
                                }
                            } catch (Exception e) {
                                out.println("<tr><td colspan='4' class='py-4 text-center text-red-300'>Error reading rentals: " + e.getMessage() + "</td></tr>");
                            }
                        } else {
                            out.println("<tr><td colspan='4' class='py-4 text-center text-gray-300'>No rentals data found.</td></tr>");
                        }
                    %>
                </tbody>
            </table>
        </div>
    </div>

    <script>
        window.onload = function() {
            anime({
                targets: '.animate-content',
                translateY: [20, 0],
                opacity: [0, 1],
                delay: anime.stagger(200),
                easing: 'easeOutExpo',
                duration: 1200
            });
        };
    </script>
</body>
</html>
